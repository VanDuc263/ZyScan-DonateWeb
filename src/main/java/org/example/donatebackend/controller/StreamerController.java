package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.StreamerRequest;
import org.example.donatebackend.dto.response.*;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.mapper.UserMapper;
import org.example.donatebackend.service.StreamerService;
import org.example.donatebackend.service.UserService;
import org.example.donatebackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/streamers")
public class StreamerController {

    @Autowired
    private StreamerService streamerService;

    @Autowired
    private UserService  userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/create")
    public ResponseEntity<AuthResponse> createStreamer(@ModelAttribute StreamerRequest req) {
        StreamerEntity streamer = streamerService.createStreamer(req);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.findByUsername(username);

        String token = jwtUtil.generateToken(username, userEntity.getRole());


        UserResponse userResponse = userMapper.toUserResponse(userEntity);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserResponse(userResponse);
        authResponse.setToken(token);


        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> updateAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("token")  String token
    ) {
        String url = streamerService.uploadStreamerAvatar(file,token);

        return ResponseEntity.ok(url);
    }

    @PostMapping("/thumbnail")
    public ResponseEntity<String> updateThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam("token")  String token
    ){
        String url = streamerService.uploadThumbStreamer(file,token);
        return ResponseEntity.ok(url);
    }
    @GetMapping("/search")
    public List<SearchStreamerResponse> searchStreamers(@RequestParam("q") String keyword) {
        return streamerService  .searchStreamers(keyword);
    }


    @GetMapping("/{token}")
    public StreamerDetailResponse getByToken(@PathVariable String token, Authentication authentication) {
        UserEntity userEntity = null;
        if(            authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())
        ) {

            String username = authentication.getName();

            userEntity =
                    userService.findByUsername(username);
        }
        return streamerService.getByDonateToken(token,userEntity);
    }

    @GetMapping("/top")
    public List<TopStreamerResponse> getTopStreamers() {
        return streamerService.getTop10Streamer();
    }

}
