package org.example.donatebackend.repository;

import org.example.donatebackend.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {
    Optional<PaymentOrderEntity> findByTransactionCode(String transactionCode);
    Optional<PaymentOrderEntity> findByAddInfo(String addInfo);
}