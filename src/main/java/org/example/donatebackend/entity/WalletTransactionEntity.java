package org.example.donatebackend.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(nullable = false)
    private String type;
    // DEPOSIT, WITHDRAW, DONATION_IN, DONATION_OUT, REFUND

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_before")
    private BigDecimal balanceBefore;

    @Column(name = "balance_after")
    private BigDecimal balanceAfter;

    @Column(name = "reference_type")
    private String referenceType;
    // DONATION / PAYMENT / SYSTEM

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private String status = "SUCCESS";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}