package org.example.donatebackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "streamer_id")
    private StreamerEntity streamer;

    private String donorName;
    @Column(name = "donor_id")
    private Long donorId;
    private Double amount;
    private String message;

    @Column(name = "status")
    private String status;



    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public Long  getDonorId() {
        return donorId;
    }

    public void setDonorId(Long  donorId) {
        this.donorId = donorId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
