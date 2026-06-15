package org.example.donatebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.donatebackend.entity.StreamerSettingsEntity;
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

    public boolean updateConfig(String username, String config) {
        Long streamerId = streamerService.getStreamerId(username);

        return settingsRepo.updateConfig(streamerId, config) > 0;
    }
    public Map<String, Object> getMyConfig(String username) {
        Long streamerId = streamerService.getStreamerId(username);

        return getConfig(streamerId);
    }



    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return Map.of(); // fallback
        }
    }
}
