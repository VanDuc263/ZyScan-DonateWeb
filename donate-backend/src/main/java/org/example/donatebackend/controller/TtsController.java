package org.example.donatebackend.controller;

import org.example.donatebackend.config.FptTtsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TtsController {
    private final FptTtsProperties fptTtsProperties;
    public TtsController(FptTtsProperties fptTtsProperties) {
        this.fptTtsProperties = fptTtsProperties;
    }

    @PostMapping("/fpt")
    public ResponseEntity<?> fptTts(@RequestBody Map<String, String> body) {
        String text = body.get("text");

        RestTemplate restTemplate = new RestTemplate();

        System.out.println(body.get("text"));

        HttpHeaders headers = new HttpHeaders();

        headers.set("api_key", fptTtsProperties.getKey());
        headers.set("voice", fptTtsProperties.getVoice());
        headers.set("speed", "0");
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));

        HttpEntity<String> request = new HttpEntity<>(text, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                fptTtsProperties.getUrl(),
                HttpMethod.POST,
                request,
                Map.class
        );
        return ResponseEntity.ok(response.getBody());
    }
}
