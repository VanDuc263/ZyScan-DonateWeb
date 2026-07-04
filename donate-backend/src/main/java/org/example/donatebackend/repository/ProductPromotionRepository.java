package org.example.donatebackend.repository;

import org.example.donatebackend.entity.ProductPromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductPromotionRepository extends JpaRepository<ProductPromotionEntity, Long> {
    List<ProductPromotionEntity> findByStreamerIdOrderByCreatedAtDescIdDesc(Long streamerId);
}
