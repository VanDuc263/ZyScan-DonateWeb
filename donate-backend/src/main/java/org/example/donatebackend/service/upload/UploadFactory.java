package org.example.donatebackend.service.upload;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Component
public class UploadFactory {
    private final Map<String,UploadStrategy> strategies;

    public UploadFactory(UserAvatarUpload user,
                         StreamerAvatarUpload streamer,
                         ThumbnailUpload thumb,
                         ProductPromotionUpload productPromotionUpload) {
        strategies = Map.of(
                "USER", user,
                "STREAMER", streamer,
                "THUMB", thumb,
                "PRODUCT_PROMOTION", productPromotionUpload
        );
    }
    public UploadStrategy get(String type) {
        UploadStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new RuntimeException("Invalid upload type");
        }
        return strategy;
    }

}
