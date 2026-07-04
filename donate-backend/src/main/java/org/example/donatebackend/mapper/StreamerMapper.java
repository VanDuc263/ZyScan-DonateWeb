package org.example.donatebackend.mapper;

import org.example.donatebackend.dto.response.StreamerDetailResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.springframework.stereotype.Component;

@Component
public class StreamerMapper {
    public StreamerDetailResponse  toStreamerDetailResponse(StreamerEntity streamer) {
        StreamerDetailResponse res = new StreamerDetailResponse();
        res.setStreamerId(streamer.getId());
        res.setDisplayName(streamer.getDisplayName());
        res.setToken(streamer.getToken());
        res.setAvatar(streamer.getAvatar());
        res.setBio(streamer.getBio());
        res.setFollowers(streamer.getFollowers());
        res.setThumb(streamer.getThumb());
        return res;
    }
}
