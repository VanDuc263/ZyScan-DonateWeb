package org.example.donatebackend.service;

import jakarta.transaction.Transactional;
import org.example.donatebackend.dto.request.ProductPromotionRequest;
import org.example.donatebackend.dto.response.ProductPromotionResponse;
import org.example.donatebackend.entity.ProductPromotionEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.ProductPromotionRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductPromotionService {

    @Autowired
    private ProductPromotionRepository productPromotionRepository;

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @Transactional
    public List<ProductPromotionResponse> saveMyPromotions(
            String username,
            List<ProductPromotionRequest> requests
    ) {
        Long streamerId = getStreamerIdByUsername(username);

        List<ProductPromotionEntity> existingItems =
                productPromotionRepository.findByStreamerIdOrderByCreatedAtDescIdDesc(streamerId);

        Map<Long, ProductPromotionEntity> existingById = existingItems.stream()
                .collect(Collectors.toMap(ProductPromotionEntity::getId, Function.identity()));

        Set<Long> retainedIds = new HashSet<>();
        List<ProductPromotionEntity> savedItems = new ArrayList<>();

        if (requests != null) {
            for (ProductPromotionRequest request : requests) {
                if (isBlankPromotion(request)) {
                    continue;
                }

                ProductPromotionEntity entity;

                if (request.getId() != null) {
                    entity = existingById.get(request.getId());
                    if (entity == null) {
                        throw new AppException(
                                ErrorCode.INVALID_REQUEST,
                                "Product promotion does not belong to this streamer"
                        );
                    }
                    retainedIds.add(entity.getId());
                } else {
                    entity = new ProductPromotionEntity();
                    entity.setStreamerId(streamerId);
                    entity.setCreatedAt(new Date());
                }

                entity.setTitle(normalize(request.getTitle()));
                entity.setUrl(normalize(request.getUrl()));
                entity.setImageUrl(normalize(request.getImageUrl()));

                ProductPromotionEntity saved = productPromotionRepository.save(entity);
                retainedIds.add(saved.getId());
                savedItems.add(saved);
            }
        }

        List<ProductPromotionEntity> staleItems = existingItems.stream()
                .filter(item -> !retainedIds.contains(item.getId()))
                .toList();

        if (!staleItems.isEmpty()) {
            productPromotionRepository.deleteAll(staleItems);
        }

        return savedItems.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductPromotionResponse> getMyPromotions(String username) {
        Long streamerId = getStreamerIdByUsername(username);

        return productPromotionRepository.findByStreamerIdOrderByCreatedAtDescIdDesc(streamerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductPromotionResponse> getPromotionsByStreamerId(Long streamerId) {
        return productPromotionRepository.findByStreamerIdOrderByCreatedAtDescIdDesc(streamerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public String uploadPromotionImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Image file is required");
        }

        return fileUploadService.upload("PRODUCT_PROMOTION", file);
    }

    private Long getStreamerIdByUsername(String username) {
        UserEntity user = userService.findByUsername(username);

        StreamerEntity streamer = streamerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Streamer not found"));

        return streamer.getId();
    }

    private boolean isBlankPromotion(ProductPromotionRequest request) {
        if (request == null) {
            return true;
        }

        return normalize(request.getTitle()) == null
                && normalize(request.getUrl()) == null
                && normalize(request.getImageUrl()) == null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductPromotionResponse toResponse(ProductPromotionEntity entity) {
        ProductPromotionResponse response = new ProductPromotionResponse();
        response.setId(entity.getId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setTitle(entity.getTitle());
        response.setUrl(entity.getUrl());
        response.setImageUrl(entity.getImageUrl());
        return response;
    }
}
