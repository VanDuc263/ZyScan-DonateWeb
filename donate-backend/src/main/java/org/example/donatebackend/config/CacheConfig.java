package org.example.donatebackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String STREAMER_BY_ID_CACHE = "streamerById";
    public static final String SYSTEM_PAYMENT_METHOD_BY_ID_CACHE = "systemPaymentMethodById";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                STREAMER_BY_ID_CACHE,
                SYSTEM_PAYMENT_METHOD_BY_ID_CACHE
        );
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(2_000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
        );
        return cacheManager;
    }
}
