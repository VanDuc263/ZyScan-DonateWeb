package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.AdminCreateUserRequest;
import org.example.donatebackend.dto.request.AdminUpdateUserRequest;
import org.example.donatebackend.dto.response.AdminUserResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.repository.StreamerRepository;
import org.example.donatebackend.repository.UserRepository;
import org.example.donatebackend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest req) {
        validateCreate(req);

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername().trim());
        user.setEmail(req.getEmail().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(trimOrNull(req.getFullName()));
        user.setAvatar(trimOrNull(req.getAvatar()));
        user.setRole(req.getRole());

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUpdateUserRequest req) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getUsername() != null && !req.getUsername().trim().isEmpty()) {
            String username = req.getUsername().trim();
            userRepository.findByUsername(username).ifPresent(existingUser -> {
                if (existingUser.getId() != user.getId()) {
                    throw new RuntimeException("Username already exists");
                }
            });
            user.setUsername(username);
        }

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            String email = req.getEmail().trim();
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                if (existingUser.getId() != user.getId()) {
                    throw new RuntimeException("Email already exists");
                }
            });
            user.setEmail(email);
        }

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getFullName() != null) {
            user.setFullName(trimOrNull(req.getFullName()));
        }

        if (req.getAvatar() != null) {
            user.setAvatar(trimOrNull(req.getAvatar()));
        }

        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        streamerRepository.findByUserId(id).ifPresent(streamer -> streamerRepository.deleteByUserId(id));
        userRepository.delete(user);
    }

    private void validateCreate(AdminCreateUserRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (req.getRole() == null) {
            throw new RuntimeException("Role is required");
        }
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail().trim())) {
            throw new RuntimeException("Email already exists");
        }
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AdminUserResponse toResponse(UserEntity user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        streamerRepository.findByUserId(user.getId()).ifPresent(streamer -> {
            response.setStreamerId(streamer.getId());
            response.setStreamerDisplayName(streamer.getDisplayName());
        });

        walletRepository.findByUser_Id(user.getId())
                .map(WalletEntity::getBalance)
                .ifPresent(response::setWalletBalance);

        return response;
    }
}
