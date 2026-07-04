package org.example.donatebackend.repository;

import org.example.donatebackend.entity.PaymentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.status = :status")
    Double sumAmountByStatus(@Param("status") String status);
}
