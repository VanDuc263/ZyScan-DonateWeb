package org.example.donatebackend.service;


import org.example.donatebackend.dto.response.FollowerResponse;
import org.example.donatebackend.dto.response.FollowingResponse;
import org.example.donatebackend.entity.FollowerEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.mapper.FollowerMapper;
import org.example.donatebackend.repository.FollowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FollowerService {

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private FollowerMapper followerMapper;



    public List<FollowerResponse> getFollowersByStreamerId(Long streamerId){
        return followerRepository.findAllByStreamerId(streamerId).stream().map(
                followerEntity -> {
                    return followerMapper.toResponse(followerEntity);

                }
        ).toList();
    }
    public List<FollowingResponse> getFollowingByUserId(UserEntity user){
        return followerRepository.findAllByFollower(user).stream().map(
                followerEntity -> {
                    return followerMapper.toFollowingResponse(followerEntity);

                }
        ).toList();
    }
    public boolean isFollowing(Long followerId, Long streamerId){
        return followerRepository
                .existsByFollower_IdAndStreamer_Id(
                        followerId,
                        streamerId
                );
    }



    public void follow(UserEntity user, StreamerEntity streamer) {
        FollowerEntity follower = new FollowerEntity();
        follower.setFollower(user);
        follower.setStreamer(streamer);
        follower.setCreatedAt(LocalDateTime.now());
        followerRepository.save(follower);
    }

    public void unfollow(UserEntity user, StreamerEntity streamer) {
        FollowerEntity follower = followerRepository.findByFollowerIdAndStreamerId(user.getId(),streamer.getId()).orElseThrow(
                () -> new AppException(ErrorCode.INTERNAL_ERROR,"USER OR STREAMER NOT FOUND")
        );
        follower.setFollower(user);
        follower.setStreamer(streamer);
        follower.setCreatedAt(LocalDateTime.now());
        followerRepository.delete(follower);
    }
}
