package org.example.donatebackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "followers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follower_streamer",
                        columnNames = {"follower_id", "streamer_id"}
                )
        }
)
public class FollowerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserEntity follower;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamer_id", nullable = false)
    private StreamerEntity streamer;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public FollowerEntity() {
    }

    public Long getId() {
        return id;
    }

    public UserEntity getFollower() {
        return follower;
    }

    public void setFollower(UserEntity follower) {
        this.follower = follower;
    }

    public StreamerEntity getStreamer() {
        return streamer;
    }

    public void setStreamer(StreamerEntity streamer) {
        this.streamer = streamer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}