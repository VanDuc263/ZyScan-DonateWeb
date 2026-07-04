package org.example.donatebackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "streamer_settings")

public class StreamerSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "streamer_id")
    private Long streamerId;

    @Column(columnDefinition = "jsonb")
    private String config;


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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
