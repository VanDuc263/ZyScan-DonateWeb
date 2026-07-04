package org.example.donatebackend.redis;

import org.example.donatebackend.dto.response.DonationResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHANNEL = "donate-channel";

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate
                          ) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(DonationResponse message) {
        try {
            redisTemplate.convertAndSend(CHANNEL, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}