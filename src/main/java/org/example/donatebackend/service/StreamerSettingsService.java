package org.example.donatebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.StreamerSettingsEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.repository.StreamerSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StreamerSettingsService {

    @Autowired
    private StreamerSettingsRepository settingsRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private StreamerService streamerService;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> getConfig(Long streamerId) {
        String config = settingsRepo.findByStreamerId(streamerId)
                .map(StreamerSettingsEntity::getConfig)
                .orElse("{}");

        return Map.of("config", parseJson(config));
    }

    public void updateConfig(Long streamerId, String config) {
        settingsRepo.updateConfig(streamerId, config);
    }
    public Map<String, Object> getMyConfig(String username) {
        UserEntity user = userService.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

//        StreamerEntity streamer = streamerService.findByUserId(user.getId());
//
//        if (streamer == null) {
//            throw new RuntimeException("Streamer not found");
//        }

        return getConfig(1L);
    }
    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return Map.of(); // fallback
        }
    }
}
