package org.example.donatebackend.repository;

import org.example.donatebackend.entity.StreamerSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface StreamerSettingsRepository
        extends JpaRepository<StreamerSettingsEntity, Long> {

    Optional<StreamerSettingsEntity> findByStreamerId(Long streamerId);

    void deleteByStreamerId(Long streamerId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE streamer_settings
        SET config = CAST(:config AS jsonb)
        WHERE streamer_id = :streamerId
    """, nativeQuery = true)
    void updateConfig(Long streamerId, String config);
}
