package org.example.donatebackend.repository;

import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity,Integer> {
    WalletTransactionEntity findByTransactionCodeAndStatus(String content, TransactionStatus transactionStatus);
}
