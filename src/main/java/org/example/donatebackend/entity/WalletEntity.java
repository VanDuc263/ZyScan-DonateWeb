package org.example.donatebackend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "frozen_balance")
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column
    private String currency = "VND";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
