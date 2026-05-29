package org.example.donatebackend.controller;

import org.example.donatebackend.dto.response.FollowerResponse;
import org.example.donatebackend.dto.response.FollowingResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.service.FollowerService;
import org.example.donatebackend.service.StreamerService;
import org.example.donatebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
public class FollowerController {

    @Autowired
    private FollowerService followerService;

    @Autowired
    private StreamerService streamerService;

    @Autowired
    private UserService userService;

    @GetMapping("/followers")
    public ResponseEntity<List<FollowerResponse>> getFollowersByStreamer(Authentication authentication){
        String username =  authentication.getName();

        Long streamerId = streamerService.getStreamerId(username);

        return ResponseEntity.ok(followerService.getFollowersByStreamerId(streamerId));
    }
    @GetMapping("/following")
    public ResponseEntity<List<FollowingResponse>> getFollowingByUser(Authentication authentication){
        String username =  authentication.getName();

        UserEntity user = userService.findByUsername(username);

        return ResponseEntity.ok(followerService.getFollowingByUserId(user));
    }
    @PostMapping("/{token}")
    public ResponseEntity<?> follow(
            @PathVariable String token,
            Authentication authentication
    ) {
        String username = authentication.getName();

        UserEntity user =
                userService.findByUsername(username);
        StreamerEntity streamer = streamerService.getStreamerIdByToken(token);

        followerService.follow(user, streamer);

        streamerService.updateFollowers(1,streamer);


        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{token}")
    public ResponseEntity<?> unfollow(
            @PathVariable String token,
            Authentication authentication
    ) {

        String username = authentication.getName();

        UserEntity user =
                userService.findByUsername(username);
        StreamerEntity streamer = streamerService.getStreamerIdByToken(token);

        followerService.unfollow(
                user,
                streamer
        );
        streamerService.updateFollowers(-1,streamer);
        return ResponseEntity.ok().build();
    }
}
