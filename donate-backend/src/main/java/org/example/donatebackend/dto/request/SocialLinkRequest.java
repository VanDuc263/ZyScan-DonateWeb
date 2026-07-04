package org.example.donatebackend.dto.request;

import org.example.donatebackend.enums.SocialPlatform;

public class SocialLinkRequest {

    private SocialPlatform platform;
    private String url;
    private Boolean visible;

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}