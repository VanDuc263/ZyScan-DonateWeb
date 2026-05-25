package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.AdminUpdateStreamerRequest;
import org.example.donatebackend.dto.response.AdminStreamerResponse;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.repository.StreamerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminStreamerService {

    @Autowired private StreamerRepository streamerRepository;
    @Autowired private AdminMapperService adminMapperService;

    @Transactional(readOnly = true)
    public List<AdminStreamerResponse> findAll() {
        return streamerRepository.findAll()
                .stream()
                .map(adminMapperService::toStreamerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminStreamerResponse findById(Long id) {
        return adminMapperService.toStreamerResponse(
                streamerRepository.findById(id).orElseThrow(() -> new RuntimeException("Streamer not found"))
        );
    }

    @Transactional
    public AdminStreamerResponse update(Long id, AdminUpdateStreamerRequest req) {
        StreamerEntity streamer = streamerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Streamer not found"));

        if (req.getDisplayName() != null && !req.getDisplayName().trim().isEmpty()) {
            streamer.setDisplayName(req.getDisplayName().trim());
        }
        if (req.getToken() != null && !req.getToken().trim().isEmpty()) {
            streamer.setToken(req.getToken().trim());
        }
        if (req.getAvatar() != null) streamer.setAvatar(trimOrNull(req.getAvatar()));
        if (req.getThumb() != null) streamer.setThumb(trimOrNull(req.getThumb()));
        if (req.getBio() != null) streamer.setBio(trimOrNull(req.getBio()));
        if (req.getFollowers() != null) streamer.setFollowers(Math.max(0, req.getFollowers()));

        return adminMapperService.toStreamerResponse(streamerRepository.save(streamer));
    }

    @Transactional
    public void delete(Long id) {
        StreamerEntity streamer = streamerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Streamer not found"));
        streamerRepository.delete(streamer);
    }

    private String trimOrNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
