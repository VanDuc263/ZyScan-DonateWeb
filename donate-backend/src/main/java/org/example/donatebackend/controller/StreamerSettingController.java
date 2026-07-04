package org.example.donatebackend.controller;


import org.example.donatebackend.service.StreamerSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class StreamerSettingController {

    @Autowired
    private StreamerSettingsService streamerSettingsService;


    @GetMapping("/me")
    public ResponseEntity<?> getSettings(Authentication authentication) {
        return ResponseEntity.ok(
                streamerSettingsService.getMyConfig(authentication.getName())
        );
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateSettings(
            Authentication authentication,
            @RequestBody String config
    ) {
        return ResponseEntity.ok(
                streamerSettingsService.updateConfig(authentication.getName(),config)
        );
    }
}
