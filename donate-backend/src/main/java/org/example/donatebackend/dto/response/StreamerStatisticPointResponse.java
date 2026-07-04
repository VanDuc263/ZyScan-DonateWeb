package org.example.donatebackend.dto.response;

import java.time.LocalDate;

public class StreamerStatisticPointResponse {
    private LocalDate date;
    private long donationCount;
    private double revenue;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getDonationCount() {
        return donationCount;
    }

    public void setDonationCount(long donationCount) {
        this.donationCount = donationCount;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
