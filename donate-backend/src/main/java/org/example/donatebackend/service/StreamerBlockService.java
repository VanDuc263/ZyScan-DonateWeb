package org.example.donatebackend.service;

import jakarta.transaction.Transactional;
import org.example.donatebackend.dto.response.StreamerBlockResponse;
import org.example.donatebackend.entity.StreamerBlockEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.StreamerBlockRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.example.donatebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StreamerBlockService {

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private StreamerBlockRepository streamerBlockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public List<StreamerBlockResponse> getMyBlockedUsers(String username) {
        Long streamerId = getStreamerIdByUsername(username);

        return streamerBlockRepository.findByStreamerIdOrderByCreatedAtDesc(streamerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StreamerBlockResponse blockUser(String username, Long userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "User id is required");
        }

        UserEntity actor = userService.findByUsername(username);
        Long streamerId = getStreamerIdByUsername(username);

        if (actor.getId() == userId) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn không thể tự chặn chính mình");
        }

        if (streamerBlockRepository.existsByStreamerIdAndUserId(streamerId, userId)) {
            return streamerBlockRepository.findByStreamerIdAndUserId(streamerId, userId)
                    .map(this::toResponse)
                    .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Block state is invalid"));
        }

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        StreamerBlockEntity entity = new StreamerBlockEntity();
        entity.setStreamerId(streamerId);
        entity.setUserId(targetUser.getId());
        entity.setCreatedAt(LocalDateTime.now());

        return toResponse(streamerBlockRepository.save(entity));
    }

    @Transactional
    public void unblockUser(String username, Long userId) {
        Long streamerId = getStreamerIdByUsername(username);
        streamerBlockRepository.deleteByStreamerIdAndUserId(streamerId, userId);
    }

    public Set<Long> getBlockedUserIds(Long streamerId) {
        return streamerBlockRepository.findByStreamerIdOrderByCreatedAtDesc(streamerId)
                .stream()
                .map(StreamerBlockEntity::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean isBlocked(Long streamerId, Long userId) {
        if (streamerId == null || userId == null) {
            return false;
        }

        return streamerBlockRepository.existsByStreamerIdAndUserId(streamerId, userId);
    }

    private Long getStreamerIdByUsername(String username) {
        Long userId = userService.findByUsername(username).getId();

        StreamerEntity streamer = streamerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Streamer not found"));

        return streamer.getId();
    }

    private StreamerBlockResponse toResponse(StreamerBlockEntity entity) {
        StreamerBlockResponse response = new StreamerBlockResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setCreatedAt(entity.getCreatedAt());

        if (entity.getUserId() != null) {
            userRepository.findById(entity.getUserId()).ifPresent(user -> {
                response.setUsername(user.getUsername());
                response.setFullName(user.getFullName());
                response.setAvatar(user.getAvatar());
            });
        }

        return response;
    }
}
