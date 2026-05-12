package org.example.donatebackend.repository;

import org.example.donatebackend.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity,Integer> {
}
