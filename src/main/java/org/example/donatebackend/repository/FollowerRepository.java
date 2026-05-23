package org.example.donatebackend.repository;

import org.example.donatebackend.entity.FollowerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<FollowerEntity,Long> {
    List<FollowerEntity> findAllByStreamerId(Long streamerId);

    List<FollowerEntity> findAllByFollower(UserEntity follower);

    boolean existsByFollower_IdAndStreamer_Id(
            Long followerId,
            Long streamerId
    );
}
