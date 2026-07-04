package org.example.donatebackend.dto.request;

public class AdminUpdateStreamerRequest {
    private String displayName;
    private String token;
    private String avatar;
    private String thumb;
    private String bio;
    private Integer followers;

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
}
