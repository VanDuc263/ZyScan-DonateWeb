package org.example.donatebackend.repository;

import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity,Long> {
    WalletTransactionEntity findByTransactionCodeAndStatus(String content, TransactionStatus transactionStatus);

    List<WalletTransactionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COALESCE(SUM(w.fee), 0) FROM WalletTransactionEntity w WHERE w.status = :status")
    BigDecimal sumFeeByStatus(@Param("status") TransactionStatus status);
}
