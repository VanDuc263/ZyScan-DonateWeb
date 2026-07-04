package org.example.donatebackend.dto.response;

import org.example.donatebackend.entity.UserEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private UserEntity.Role role;
    private LocalDateTime createdAt;
    private Long streamerId;
    private String streamerDisplayName;
    private BigDecimal walletBalance;

    public AdminUserResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public UserEntity.Role getRole() { return role; }
    public void setRole(UserEntity.Role role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getStreamerId() { return streamerId; }
    public void setStreamerId(Long streamerId) { this.streamerId = streamerId; }
    public String getStreamerDisplayName() { return streamerDisplayName; }
    public void setStreamerDisplayName(String streamerDisplayName) { this.streamerDisplayName = streamerDisplayName; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
}
