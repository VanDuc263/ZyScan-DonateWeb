package org.example.donatebackend.dto.response;

import java.util.List;

public class StreamerDetailResponse {
    private Long streamerId;
    private String token;
    private String displayName;
    private String avatar;
    private String thumb;
    private String bio;
    private int followers;
    private String qrUrl;
    private boolean following;
    private List<StreamerSocialLinkResponse> socialLinks;
    private List<ProductPromotionResponse> productPromotions;


    public Long getStreamerId() {
        return streamerId;
    }

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public List<StreamerSocialLinkResponse> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<StreamerSocialLinkResponse> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public List<ProductPromotionResponse> getProductPromotions() {
        return productPromotions;
    }

    public void setProductPromotions(List<ProductPromotionResponse> productPromotions) {
        this.productPromotions = productPromotions;
    }
}
