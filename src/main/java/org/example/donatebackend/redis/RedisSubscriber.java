package org.example.donatebackend.redis;

import org.example.donatebackend.dto.response.DonationResponse;
import org.example.donatebackend.service.WebSocketService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RedisSubscriber implements MessageListener {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public RedisSubscriber(WebSocketService webSocketService,
                           ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());

            DonationResponse donation =
                    objectMapper.readValue(json, DonationResponse.class);

            webSocketService.sendDonateAlert(
                    donation.getStreamerId(),
                    donation
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}