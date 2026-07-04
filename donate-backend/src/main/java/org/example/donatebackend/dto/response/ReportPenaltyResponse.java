package org.example.donatebackend.dto.response;

import java.time.LocalDateTime;

public class ReportPenaltyResponse {

    private Long userId;
    private String username;
    private Integer falseReportCount;
    private LocalDateTime blockedUntil;
    private Boolean blocked;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFalseReportCount() {
        return falseReportCount;
    }

    public void setFalseReportCount(Integer falseReportCount) {
        this.falseReportCount = falseReportCount;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }
}
