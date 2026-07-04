package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.AdminChartPointResponse;
import org.example.donatebackend.dto.response.AdminStatsResponse;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.example.donatebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminOverviewService {

    @Autowired private UserRepository userRepository;
    @Autowired private StreamerRepository streamerRepository;
    @Autowired private DonationRepository donationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private SystemPaymentMethodRepository systemPaymentMethodRepository;
    @Autowired private AdminMapperService adminMapperService;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        AdminStatsResponse res = new AdminStatsResponse();

        long totalDonations = donationRepository.count();
        long successDonations = donationRepository.countByStatus("SUCCESS");
        long pendingDonations = donationRepository.countByStatus("PENDING");

        res.setTotalUsers(userRepository.count());
        res.setTotalStreamers(streamerRepository.count());
        res.setTotalDonations(totalDonations);
        res.setSuccessDonations(successDonations);
        res.setPendingDonations(pendingDonations);
        res.setTotalRevenue(donationRepository.sumAmountByStatus("SUCCESS"));
        res.setTotalWalletBalance(walletRepository.sumBalance());
        res.setTotalSystemFee(walletTransactionRepository.sumFeeByStatus(TransactionStatus.SUCCESS));
        res.setPendingWalletTransactions(walletTransactionRepository.countByStatus(TransactionStatus.PENDING));
        res.setActivePaymentMethods(systemPaymentMethodRepository.countByIsActiveTrue());
        res.setSuccessRate(totalDonations == 0 ? 0D : Math.round((successDonations * 10000D / totalDonations)) / 100D);
        res.setRevenueChart(buildRevenueChart());
        res.setUserGrowth(buildUserGrowth());
        res.setLatestDonations(
                donationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 8))
                        .stream()
                        .map(adminMapperService::toDonationResponse)
                        .toList()
        );

        return res;
    }

    private List<AdminChartPointResponse> buildRevenueChart() {
        Map<YearMonth, Double> buckets = lastSevenMonthsDouble();
        for (Donation d : donationRepository.findAll()) {
            if (d.getCreatedAt() == null || !"SUCCESS".equalsIgnoreCase(d.getStatus())) continue;
            YearMonth ym = YearMonth.from(d.getCreatedAt());
            if (buckets.containsKey(ym)) {
                buckets.put(ym, buckets.get(ym) + (d.getAmount() == null ? 0D : d.getAmount()));
            }
        }

        List<AdminChartPointResponse> result = new ArrayList<>();
        buckets.forEach((ym, amount) -> result.add(new AdminChartPointResponse("T" + ym.getMonthValue(), amount, 0L)));
        return result;
    }

    private List<AdminChartPointResponse> buildUserGrowth() {
        Map<YearMonth, Long> buckets = lastSevenMonthsLong();
        for (UserEntity u : userRepository.findAll()) {
            if (u.getCreatedAt() == null) continue;
            YearMonth ym = YearMonth.from(u.getCreatedAt());
            if (buckets.containsKey(ym)) {
                buckets.put(ym, buckets.get(ym) + 1);
            }
        }

        List<AdminChartPointResponse> result = new ArrayList<>();
        buckets.forEach((ym, count) -> result.add(new AdminChartPointResponse("T" + ym.getMonthValue(), 0D, count)));
        return result;
    }

    private Map<YearMonth, Double> lastSevenMonthsDouble() {
        Map<YearMonth, Double> map = new LinkedHashMap<>();
        YearMonth now = YearMonth.from(LocalDate.now());
        for (int i = 6; i >= 0; i--) {
            map.put(now.minusMonths(i), 0D);
        }
        return map;
    }

    private Map<YearMonth, Long> lastSevenMonthsLong() {
        Map<YearMonth, Long> map = new LinkedHashMap<>();
        YearMonth now = YearMonth.from(LocalDate.now());
        for (int i = 6; i >= 0; i--) {
            map.put(now.minusMonths(i), 0L);
        }
        return map;
    }
}
