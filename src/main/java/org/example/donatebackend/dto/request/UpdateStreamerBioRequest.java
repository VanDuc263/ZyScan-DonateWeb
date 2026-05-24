package org.example.donatebackend.dto.request;

import java.util.List;

public class UpdateStreamerBioRequest {

    private String displayName;
    private String bio;
    private List<SocialLinkRequest> socialLinks;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<SocialLinkRequest> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkRequest> socialLinks) {
        this.socialLinks = socialLinks;
    }
}