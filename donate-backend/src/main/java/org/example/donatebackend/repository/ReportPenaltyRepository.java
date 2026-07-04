package org.example.donatebackend.repository;

import org.example.donatebackend.entity.ReportPenaltyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportPenaltyRepository extends JpaRepository<ReportPenaltyEntity, Long> {

    Optional<ReportPenaltyEntity> findByUser_Id(Long userId);
}
