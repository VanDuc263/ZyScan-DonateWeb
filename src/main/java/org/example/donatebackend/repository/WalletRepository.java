package org.example.donatebackend.repository;

import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository  extends JpaRepository<WalletEntity, Integer> {

    Optional<WalletEntity> findByUser(UserEntity user);
}
