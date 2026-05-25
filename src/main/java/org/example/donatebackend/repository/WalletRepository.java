package org.example.donatebackend.repository;

import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    Optional<WalletEntity> findByUser(UserEntity user);

    Optional<WalletEntity> findByUser_Id(Long userId);

    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM WalletEntity w")
    BigDecimal sumBalance();
}
