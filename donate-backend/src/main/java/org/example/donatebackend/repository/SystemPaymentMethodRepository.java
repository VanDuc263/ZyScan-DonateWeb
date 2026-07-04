package org.example.donatebackend.repository;

import org.example.donatebackend.entity.SystemPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SystemPaymentMethodRepository extends JpaRepository<SystemPaymentMethod, Long> {

    @Query("SELECT m FROM SystemPaymentMethod m WHERE m.isActive = true")
    List<SystemPaymentMethod> findByIsActiveTrue();

    List<SystemPaymentMethod> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(m) FROM SystemPaymentMethod m WHERE m.isActive = true")
    long countByIsActiveTrue();
}

