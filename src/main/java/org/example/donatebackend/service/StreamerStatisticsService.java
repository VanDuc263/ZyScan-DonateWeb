package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.StreamerStatisticPointResponse;
import org.example.donatebackend.dto.response.StreamerStatisticsResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.DonationRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class StreamerStatisticsService {

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserService userService;

    public StreamerStatisticsResponse getMyStatistics(
            String username,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date must not be after end date");
        }

        Long streamerId = getStreamerIdByUsername(username);
        StreamerEntity streamer = streamerRepository.findById(streamerId)
                .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Streamer not found"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = donationRepository.getDailyStatistics(
                streamerId,
                startDateTime,
                endDateTime
        );

        List<StreamerStatisticPointResponse> dailyStats = new ArrayList<>();
        long totalDonations = 0;
        double totalRevenue = 0;
        LocalDate bestRevenueDate = null;
        double bestRevenueAmount = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            StreamerStatisticPointResponse point = new StreamerStatisticPointResponse();
            point.setDate(date);
            point.setDonationCount(0);
            point.setRevenue(0);
            dailyStats.add(point);
        }

        for (Object[] row : rows) {
            LocalDate date = resolveLocalDate(row[0]);
            long donationCount = ((Number) row[1]).longValue();
            double revenue = ((Number) row[2]).doubleValue();

            int index = (int) ChronoUnit.DAYS.between(startDate, date);
            if (index >= 0 && index < dailyStats.size()) {
                dailyStats.get(index).setDonationCount(donationCount);
                dailyStats.get(index).setRevenue(revenue);
            }

            totalDonations += donationCount;
            totalRevenue += revenue;

            if (bestRevenueDate == null || revenue > bestRevenueAmount) {
                bestRevenueDate = date;
                bestRevenueAmount = revenue;
            }
        }

        StreamerStatisticsResponse response = new StreamerStatisticsResponse();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalDonations(totalDonations);
        response.setTotalRevenue(totalRevenue);
        response.setTotalFollowers(streamer.getFollowers());
        response.setBestRevenueDate(bestRevenueDate);
        response.setBestRevenueAmount(bestRevenueAmount);
        response.setDailyStats(dailyStats);
        return response;
    }

    private Long getStreamerIdByUsername(String username) {
        Long userId = userService.findByUsername(username).getId();

        return streamerRepository.findByUserId(userId)
                .map(StreamerEntity::getId)
                .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Streamer not found"));
    }

    private LocalDate resolveLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }

        return LocalDate.parse(String.valueOf(value));
    }
}
