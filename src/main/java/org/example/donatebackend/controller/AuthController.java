package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.LoginRequest;
import org.example.donatebackend.dto.request.RegisterRequest;
import org.example.donatebackend.dto.response.AuthResponse;
import org.example.donatebackend.dto.response.StreamerDetailResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.mapper.StreamerMapper;
import org.example.donatebackend.service.AuthService;
import org.example.donatebackend.service.GoogleTokenVerifier;
import org.example.donatebackend.service.StreamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private StreamerService  streamerService;

    @Autowired
    private StreamerMapper streamerMapper;;

    @PostMapping("/register")
    public Map<String, String> registerUser(@RequestBody RegisterRequest req) {
        authService.registerUser(
                req.getUsername(),
                req.getEmail(),
                req.getPassword()
        );
        return Map.of("message", "Register success");
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody LoginRequest req) {
        AuthResponse authResponse = authService.login(
                req.getUsername(),
                req.getPassword()
        );

        return Map.of(
                "token",authResponse.getToken()
                ,"user",authResponse);
    }


@GetMapping("/me")
public Map<String, Object> me(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");

    String username = authService.extractUsername(token);
    UserEntity user = authService.getUserByUsername(username);

    StreamerEntity streamerEntity = streamerService.findByUserId(user.getId());
    StreamerDetailResponse streamerDetailResponse = null;

    if (streamerEntity != null) {
        streamerDetailResponse = streamerMapper.toStreamerDetailResponse(streamerEntity);
    }

    Map<String, Object> userMap = new java.util.HashMap<>();
    userMap.put("userId", user.getId());
    userMap.put("username", user.getUsername());
    userMap.put("email", user.getEmail());
    userMap.put("role", user.getRole() != null ? user.getRole().name() : null);
    userMap.put("avatar", user.getAvatar());
    userMap.put("fullName", user.getFullName());

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("user", userMap);
    result.put("streamer", streamerDetailResponse);

    return result;
}

    @PostMapping("/google")
    public Map<String, Object> google(@RequestBody Map<String,String> req) {
        try {
            String idToken = req.get("credential");
            var payload = googleTokenVerifier.verify(idToken);


            String email = payload.getEmail();
            String name = (String) payload.get("name");

            AuthResponse authResponse = authService.findOrCreateGoogleUser(name,email);

            StreamerEntity streamer = streamerService.findByUserId(authResponse.getUserResponse().getId());
            StreamerDetailResponse streamerDetailResponse = streamerMapper.toStreamerDetailResponse(streamer);
            authResponse.setStreamerDetailReponse(streamerDetailResponse);

            return Map.of(
                    "token", authResponse.getToken(),
                    "user",authResponse.getUserResponse(),
                    "streamer",authResponse.getStreamerDetailReponse()
            );


        }catch (Exception e){
            return Map.of("error", "Invalid Google token");

        }

    }
}