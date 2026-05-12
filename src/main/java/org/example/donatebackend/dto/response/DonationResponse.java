package org.example.donatebackend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class DonationResponse {
    private Long id;
    private Long streamerId;
    private String streamerName;
    private String streamerToken;
    private String streamerAvatar;

    private Long donorId;
    private String donorName;
    private Double amount;
    private String message;

    private String status;
    private LocalDateTime createdAt;
    private String content;
    private String referenceCode;

    private List<TopDonorResponse> topDonors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStreamerId() {
        return streamerId;
    }

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public String getStreamerName() {
        return streamerName;
    }

    public void setStreamerName(String streamerName) {
        this.streamerName = streamerName;
    }

    public String getStreamerToken() {
        return streamerToken;
    }

    public void setStreamerToken(String streamerToken) {
        this.streamerToken = streamerToken;
    }

    public String getStreamerAvatar() {
        return streamerAvatar;
    }

    public void setStreamerAvatar(String streamerAvatar) {
        this.streamerAvatar = streamerAvatar;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public List<TopDonorResponse> getTopDonors() {
        return topDonors;
    }

    public void setTopDonors(List<TopDonorResponse> topDonors) {
        this.topDonors = topDonors;
    }
}
