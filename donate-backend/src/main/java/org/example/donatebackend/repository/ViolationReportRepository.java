package org.example.donatebackend.repository;

import org.example.donatebackend.entity.ViolationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViolationReportRepository extends JpaRepository<ViolationReportEntity, Long> {

    List<ViolationReportEntity> findAllByOrderByCreatedAtDesc();

    List<ViolationReportEntity> findByReporter_IdOrderByCreatedAtDesc(Long reporterId);

    List<ViolationReportEntity> findByStatusOrderByCreatedAtDesc(ViolationReportEntity.ReportStatus status);

    List<ViolationReportEntity> findByTargetTypeOrderByCreatedAtDesc(ViolationReportEntity.TargetType targetType);

    List<ViolationReportEntity> findByStatusAndTargetTypeOrderByCreatedAtDesc(
            ViolationReportEntity.ReportStatus status,
            ViolationReportEntity.TargetType targetType
    );

    boolean existsByReporter_IdAndTargetTypeAndTargetIdAndStatusIn(
            Long reporterId,
            ViolationReportEntity.TargetType targetType,
            Long targetId,
            List<ViolationReportEntity.ReportStatus> statuses
    );
}
