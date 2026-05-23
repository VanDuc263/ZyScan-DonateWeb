package org.example.donatebackend.mapper;

import org.example.donatebackend.dto.response.FollowerResponse;
import org.example.donatebackend.dto.response.FollowingResponse;
import org.example.donatebackend.entity.FollowerEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.springframework.stereotype.Component;

@Component
public class FollowerMapper {
    public FollowerResponse toResponse(FollowerEntity f) {
        FollowerResponse res = new FollowerResponse();

        res.setId(f.getId());
        res.setAvatar(f.getFollower().getAvatar());
        res.setFollowerId(f.getFollower().getId());
        res.setFollowerName(f.getFollower().getFullName());
        res.setStreamerId(f.getStreamer().getId());
        res.setStreamerName(f.getStreamer().getDisplayName());
        res.setCreatedAt(f.getCreatedAt());

        return res;
    }
    public FollowingResponse toFollowingResponse(FollowerEntity follow) {
        StreamerEntity streamer = follow.getStreamer();

        FollowingResponse res = new FollowingResponse();
        res.setFollowId(follow.getId());
        res.setStreamerId(streamer.getId());
        res.setStreamerName(streamer.getDisplayName());
        res.setToken(streamer.getToken());
        res.setAvatar(streamer.getAvatar());
        res.setThumb(streamer.getThumb());
        res.setBio(streamer.getBio());
        res.setFollowedAt(follow.getCreatedAt());

        return res;
    }
}
