package org.example.donatebackend.repository;

import org.example.donatebackend.entity.StreamerSocialLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamerSocialLinkRepository
        extends JpaRepository<StreamerSocialLinkEntity, Long> {

    List<StreamerSocialLinkEntity> findByStreamerIdAndIsVisibleTrue(Long streamerId);

    List<StreamerSocialLinkEntity> findByStreamerId(Long streamerId);

    void deleteByStreamerId(Long streamerId);
}
