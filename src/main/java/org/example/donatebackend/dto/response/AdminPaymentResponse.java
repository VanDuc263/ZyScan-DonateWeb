package org.example.donatebackend.dto.response;

import java.time.LocalDateTime;

public class AdminPaymentResponse {
    private Long id;
    private Long donationId;
    private String provider;
    private String transactionCode;
    private String status;
    private LocalDateTime createdAt;
    private Long streamerId;
    private Long donorId;
    private String donorName;
    private Double amount;
    private String message;
    private String bankCode;
    private String bankAccountNo;
    private String bankAccountName;
    private String addInfo;
    private String qrUrl;
    private LocalDateTime paidAt;
    private Boolean donationCreated;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getStreamerId() { return streamerId; }
    public void setStreamerId(Long streamerId) { this.streamerId = streamerId; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBankAccountNo() { return bankAccountNo; }
    public void setBankAccountNo(String bankAccountNo) { this.bankAccountNo = bankAccountNo; }
    public String getBankAccountName() { return bankAccountName; }
    public void setBankAccountName(String bankAccountName) { this.bankAccountName = bankAccountName; }
    public String getAddInfo() { return addInfo; }
    public void setAddInfo(String addInfo) { this.addInfo = addInfo; }
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public Boolean getDonationCreated() { return donationCreated; }
    public void setDonationCreated(Boolean donationCreated) { this.donationCreated = donationCreated; }
}
