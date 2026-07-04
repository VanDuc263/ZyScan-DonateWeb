package org.example.donatebackend.dto.response;

import java.time.LocalDate;
import java.util.List;

public class StreamerStatisticsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDonations;
    private double totalRevenue;
    private int totalFollowers;
    private LocalDate bestRevenueDate;
    private double bestRevenueAmount;
    private List<StreamerStatisticPointResponse> dailyStats;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(long totalDonations) {
        this.totalDonations = totalDonations;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(int totalFollowers) {
        this.totalFollowers = totalFollowers;
    }

    public LocalDate getBestRevenueDate() {
        return bestRevenueDate;
    }

    public void setBestRevenueDate(LocalDate bestRevenueDate) {
        this.bestRevenueDate = bestRevenueDate;
    }

    public double getBestRevenueAmount() {
        return bestRevenueAmount;
    }

    public void setBestRevenueAmount(double bestRevenueAmount) {
        this.bestRevenueAmount = bestRevenueAmount;
    }

    public List<StreamerStatisticPointResponse> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(List<StreamerStatisticPointResponse> dailyStats) {
        this.dailyStats = dailyStats;
    }
}
