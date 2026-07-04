package org.example.donatebackend.entity;

import jakarta.persistence.*;
import org.example.donatebackend.enums.SocialPlatform;

import java.util.Date;

@Entity
@Table(name = "streamer_social_links")
public class StreamerSocialLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "streamer_id", nullable = false)
    private Long streamerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "streamer_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
    )
    private StreamerEntity streamer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialPlatform platform;

    @Column(nullable = false)
    private String url;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public Long getStreamerId() {
        return streamerId;
    }

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public StreamerEntity getStreamer() {
        return streamer;
    }

    public void setStreamer(StreamerEntity streamer) {
        this.streamer = streamer;
    }

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
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}