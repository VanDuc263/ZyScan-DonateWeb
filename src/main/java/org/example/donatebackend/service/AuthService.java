package org.example.donatebackend.service;

import com.google.common.base.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.donatebackend.dto.response.AuthResponse;
import org.example.donatebackend.dto.response.UserResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.repository.UserRepository;
import org.example.donatebackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.donatebackend.entity.UserEntity.Role;

import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    public void registerUser(String username,String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setEmail(email);
        userEntity.setRole(Role.USER);

        userRepository.save(userEntity);
    }

    public AuthResponse login(String username,String password) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if(!passwordEncoder.matches(password,userEntity.getPassword()))
        {
            throw new RuntimeException("invalid password");
        }
        AuthResponse authResponse = new AuthResponse();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userEntity.getId());
        userResponse.setUsername(userEntity.getUsername());
        userResponse.setFullName(userEntity.getFullName());
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setRole(userEntity.getRole());
        userResponse.setAvatar(userEntity.getAvatar());

        String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole());

        authResponse.setUserResponse(userResponse);
        authResponse.setToken(token);

        return authResponse;
    }

    public void updateRole(Long userId, Role role) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userEntity.setRole(role);
        userRepository.save(userEntity);
    }
    public AuthResponse findOrCreateGoogleUser(String username, String email,String picture) {
        UserEntity userEntity=  userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String randomPassword = UUID.randomUUID().toString();

                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setUsername(generateUsername(email));
                    newUser.setPassword(passwordEncoder.encode(randomPassword));
                    newUser.setRole(Role.USER);
                    newUser.setFullName(username);
                    newUser.setAvatar(picture);
                    return userRepository.save(newUser);
                });
        AuthResponse authResponse = new AuthResponse();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userEntity.getId());
        userResponse.setUsername(userEntity.getUsername());
        userResponse.setFullName(userEntity.getFullName());
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setRole(userEntity.getRole());
        userResponse.setAvatar(userEntity.getAvatar());

        String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole());

        authResponse.setUserResponse(userResponse);
        authResponse.setToken(token);
        return authResponse;

    }
    public String createToken(String username,Role role) {
        return jwtUtil.generateToken(username,role);
    }
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0]
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        String username;

        do {
            username = baseUsername +
                    RandomStringUtils.randomNumeric(4);
        } while (userRepository.existsByUsername(username));

        return username;
    }


}
