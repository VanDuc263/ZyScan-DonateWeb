package org.example.donatebackend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalStreamers;
    private Long totalDonations;
    private Long successDonations;
    private Long pendingDonations;
    private Double totalRevenue;
    private BigDecimal totalWalletBalance;
    private BigDecimal totalSystemFee;
    private Long pendingWalletTransactions;
    private Long activePaymentMethods;
    private Double successRate;
    private List<AdminChartPointResponse> revenueChart;
    private List<AdminChartPointResponse> userGrowth;
    private List<AdminDonationResponse> latestDonations;

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
    public Long getTotalStreamers() { return totalStreamers; }
    public void setTotalStreamers(Long totalStreamers) { this.totalStreamers = totalStreamers; }
    public Long getTotalDonations() { return totalDonations; }
    public void setTotalDonations(Long totalDonations) { this.totalDonations = totalDonations; }
    public Long getSuccessDonations() { return successDonations; }
    public void setSuccessDonations(Long successDonations) { this.successDonations = successDonations; }
    public Long getPendingDonations() { return pendingDonations; }
    public void setPendingDonations(Long pendingDonations) { this.pendingDonations = pendingDonations; }
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getTotalWalletBalance() { return totalWalletBalance; }
    public void setTotalWalletBalance(BigDecimal totalWalletBalance) { this.totalWalletBalance = totalWalletBalance; }
    public BigDecimal getTotalSystemFee() { return totalSystemFee; }
    public void setTotalSystemFee(BigDecimal totalSystemFee) { this.totalSystemFee = totalSystemFee; }
    public Long getPendingWalletTransactions() { return pendingWalletTransactions; }
    public void setPendingWalletTransactions(Long pendingWalletTransactions) { this.pendingWalletTransactions = pendingWalletTransactions; }
    public Long getActivePaymentMethods() { return activePaymentMethods; }
    public void setActivePaymentMethods(Long activePaymentMethods) { this.activePaymentMethods = activePaymentMethods; }
    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    public List<AdminChartPointResponse> getRevenueChart() { return revenueChart; }
    public void setRevenueChart(List<AdminChartPointResponse> revenueChart) { this.revenueChart = revenueChart; }
    public List<AdminChartPointResponse> getUserGrowth() { return userGrowth; }
    public void setUserGrowth(List<AdminChartPointResponse> userGrowth) { this.userGrowth = userGrowth; }
    public List<AdminDonationResponse> getLatestDonations() { return latestDonations; }
    public void setLatestDonations(List<AdminDonationResponse> latestDonations) { this.latestDonations = latestDonations; }
}
