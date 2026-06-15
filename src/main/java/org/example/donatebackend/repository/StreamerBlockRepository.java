package org.example.donatebackend.repository;

import org.example.donatebackend.entity.StreamerBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreamerBlockRepository extends JpaRepository<StreamerBlockEntity, Long> {
    List<StreamerBlockEntity> findByStreamerIdOrderByCreatedAtDesc(Long streamerId);
    Optional<StreamerBlockEntity> findByStreamerIdAndUserId(Long streamerId, Long userId);
    boolean existsByStreamerIdAndUserId(Long streamerId, Long userId);
    void deleteByStreamerIdAndUserId(Long streamerId, Long userId);
}
