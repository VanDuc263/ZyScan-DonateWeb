package org.example.donatebackend.dto.response;

import java.time.LocalDateTime;
import java.util.Date;

public class AdminStreamerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String token;
    private String avatar;
    private String thumb;
    private String bio;
    private Integer followers;
    private Date createdAt;
    private Double totalReceived;
    private Long donationCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getThumb() { return thumb; }
    public void setThumb(String thumb) { this.thumb = thumb; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Integer getFollowers() { return followers; }
    public void setFollowers(Integer followers) { this.followers = followers; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Double getTotalReceived() { return totalReceived; }
    public void setTotalReceived(Double totalReceived) { this.totalReceived = totalReceived; }
    public Long getDonationCount() { return donationCount; }
    public void setDonationCount(Long donationCount) { this.donationCount = donationCount; }
}
