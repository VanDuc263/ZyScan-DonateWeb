package org.example.donatebackend.repository;

import org.example.donatebackend.entity.SystemPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemPaymentMethodRepository
        extends JpaRepository<SystemPaymentMethod, Long> {

    List<SystemPaymentMethod> findByIsActiveTrue();
}
