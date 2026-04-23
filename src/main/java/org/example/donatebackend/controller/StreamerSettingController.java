package org.example.donatebackend.controller;


import org.example.donatebackend.service.StreamerSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
