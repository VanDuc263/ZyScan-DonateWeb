package org.example.donatebackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawResponse {

    public WithdrawResponse() {
    }

    public WithdrawResponse(String transactionCode, BigDecimal amount, BigDecimal balance, BigDecimal frozenBalance, String status, LocalDateTime createdAt) {
        this.transactionCode = transactionCode;
        this.amount = amount;
        this.balance = balance;
        this.frozenBalance = frozenBalance;
        this.status = status;
        this.createdAt = createdAt;
    }

    private String transactionCode;

    private BigDecimal amount;

    private BigDecimal balance;

    private BigDecimal frozenBalance;

    private String status;

    private LocalDateTime createdAt;

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(BigDecimal frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
