package org.example.donatebackend.dto.response;

public class PaymentOrderStatusResponse {
    private String orderCode;
    private String status;
    private Double amount;
    private Boolean donationCreated;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getDonationCreated() {
        return donationCreated;
    }

    public void setDonationCreated(Boolean donationCreated) {
        this.donationCreated = donationCreated;
    }
}