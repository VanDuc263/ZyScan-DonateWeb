package org.example.donatebackend.service;

import jakarta.transaction.Transactional;
import org.example.donatebackend.dto.request.SocialLinkRequest;
import org.example.donatebackend.dto.request.StreamerRequest;
import org.example.donatebackend.dto.request.UpdateStreamerBioRequest;
import org.example.donatebackend.dto.response.*;
import org.example.donatebackend.entity.NotificationEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.StreamerSocialLinkEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.StreamerRepository;
import org.example.donatebackend.repository.StreamerSocialLinkRepository;
import org.example.donatebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class StreamerService {
    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentAccountService paymentAccountService;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private StreamerSocialLinkRepository  streamerSocialLinkRepository;

    @Autowired
    private ProductPromotionService productPromotionService;


    public StreamerEntity createStreamer(StreamerRequest request){
        String username = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,"User not found"));

        if(streamerRepository.findByToken(request.getToken()) != null){
            throw new AppException(ErrorCode.TOKEN_ALREADY_EXISTS,"Token already exists");
        }
        if(streamerRepository.findByUserId(userEntity.getId()).isPresent()){
            throw new AppException(ErrorCode.STREAMER_ALREADY_EXISTS,"streamer already exists");
        }


        StreamerEntity s = new StreamerEntity();
        s.setUserId(userEntity.getId());
        s.setDisplayName(request.getDisplayName());
        s.setToken(request.getToken());
        s.setCreatedAt(new Date(new Date().getTime()));

        StreamerEntity streamer =  streamerRepository.save(s);
        uploadStreamerAvatar(request.getFile(), streamer.getToken());

        userEntity.setRole(UserEntity.Role.STREAMER);
        userRepository.save(userEntity);

        notificationService.createNotification(
                userEntity.getId(),
                NotificationEntity.NotificationType.STREAMER,
                "Tạo trang ZyScan thành công",
                "Trang nhận donate của bạn đã được tạo",
                "/account/profile",
                null
        );

        return streamer;
    }

    public StreamerDetailResponse getByDonateToken(String donateToken,UserEntity user){
        StreamerEntity streamer =  streamerRepository.findByToken(donateToken);

        if(streamer == null){
            new Throwable("streamer not found");
        }

        StreamerDetailResponse streamerDetailReponse = new StreamerDetailResponse();

        streamerDetailReponse.setStreamerId(streamer.getId());
        streamerDetailReponse.setDisplayName(streamer.getDisplayName());
        streamerDetailReponse.setAvatar(streamer.getAvatar());
        streamerDetailReponse.setThumb(streamer.getThumb());
        streamerDetailReponse.setFollowers(streamer.getFollowers());
        streamerDetailReponse.setToken(streamer.getToken());


        if(user != null){
            boolean following = followerService.isFollowing(user.getId(),streamer.getId());
            streamerDetailReponse.setFollowing(following);
        }

        String qrUrl = paymentAccountService.getQrUrlByStreamerId(streamer.getId());
        streamerDetailReponse.setQrUrl(qrUrl);

        List<StreamerSocialLinkEntity> streamerSocialLinkEntities = streamerSocialLinkRepository.findByStreamerId(streamer.getId());

        List<StreamerSocialLinkResponse>  streamerSocialLinkResponses = streamerSocialLinkEntities.stream().map(
                streamerSocialLinkEntity -> {
                    StreamerSocialLinkResponse dto = new StreamerSocialLinkResponse();
                    dto.setPlatform(streamerSocialLinkEntity.getPlatform().toString());
                    dto.setUrl(streamerSocialLinkEntity.getUrl());
                    dto.setVisible(streamerSocialLinkEntity.getVisible());
                    return dto;
                }
        ).toList();

        streamerDetailReponse.setSocialLinks(streamerSocialLinkResponses);
        streamerDetailReponse.setProductPromotions(
                productPromotionService.getPromotionsByStreamerId(streamer.getId())
        );

        return streamerDetailReponse;
    }
    public List<TopStreamerResponse> getTop10Streamer() {

        List<Object[]> res = streamerRepository.findTopStreamers(PageRequest.of(0, 6));

        return res.stream().map(r -> {
            TopStreamerResponse dto = new TopStreamerResponse();

            dto.setStreamerId((Long) r[0]);
            dto.setDisplayName((String) r[1]);
            dto.setTotalAmount(((Number) r[2]).doubleValue());
            dto.setAvatar((String) r[3]);
            dto.setToken((String) r[4]);


            return dto;
        }).toList();
    }
    public List<SearchStreamerResponse> searchStreamers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return streamerRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(this::toSearchResponse)
                .toList();
    }

    private SearchStreamerResponse toSearchResponse(StreamerEntity streamer) {
        SearchStreamerResponse response = new SearchStreamerResponse();
        response.setStreamerId(streamer.getId());
        response.setDisplayName(streamer.getDisplayName());
        response.setToken(streamer.getToken());
        response.setAvatar(streamer.getAvatar());
        response.setThumb(streamer.getThumb());
        response.setFollowers(streamer.getFollowers());
        return response;
    }

    public StreamerEntity findByUserId(Long userId){
        return streamerRepository.findByUserId(userId).orElse(null);
    }

    public StreamerEntity updateAvatar(String token, String url) {
        StreamerEntity streamer = streamerRepository.findByToken(token);
        streamer.setAvatar(url);
        return streamerRepository.save(streamer);
    }

    public StreamerEntity updateThumb(String token, String url) {
        StreamerEntity streamer = streamerRepository.findByToken(token);
        streamer.setThumb(url);
        return streamerRepository.save(streamer);
    }

    public String uploadStreamerAvatar(MultipartFile file, String token) {
        String url = fileUploadService.upload("STREAMER", file);
        updateAvatar(token, url);
        return url;
    }

    public String uploadThumbStreamer(MultipartFile file, String token) {
        String url = fileUploadService.upload("THUMB", file);
        updateThumb(token, url);
        return url;
    }

    public String uploadProductPromotionImage(MultipartFile file) {
        return fileUploadService.upload("PRODUCT_PROMOTION", file);
    }

    public Long getStreamerId(String username) {
        UserEntity user = userService.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        StreamerEntity streamer = findByUserId(user.getId());

        if (streamer == null) {
            throw new RuntimeException("Streamer not found");
        }
        return streamer.getId();
    }
    public StreamerEntity getStreamerIdByToken(String token) {
        StreamerEntity streamer = streamerRepository.findByToken(token);

        if (streamer == null) {
            throw new RuntimeException("Streamer not found");
        }
        return streamer;
    }

    public StreamerProfileResponse getBio(Long streamerId) {
        String qrUrl = paymentAccountService.getQrUrlByStreamerId(streamerId);

        StreamerEntity streamer = streamerRepository.findById(streamerId).orElse(new StreamerEntity());

        StreamerProfileResponse streamerProfileResponse = new StreamerProfileResponse();
        streamerProfileResponse.setStreamerId(streamer.getId());
        streamerProfileResponse.setDisplayName(streamer.getDisplayName());
        streamerProfileResponse.setToken(streamer.getToken());
        streamerProfileResponse.setAvatar(streamer.getAvatar());
        streamerProfileResponse.setThumb(streamer.getThumb());
        streamerProfileResponse.setFollowers(streamer.getFollowers());
        streamerProfileResponse.setQrUrl(qrUrl);
        streamerProfileResponse.setBio(streamer.getBio());


        List<StreamerSocialLinkEntity> streamerSocialLinkEntities = streamerSocialLinkRepository.findByStreamerId(streamerId);

        List<StreamerSocialLinkResponse>  streamerSocialLinkResponses = streamerSocialLinkEntities.stream().map(
                streamerSocialLinkEntity -> {
                    StreamerSocialLinkResponse dto = new StreamerSocialLinkResponse();
                    dto.setPlatform(streamerSocialLinkEntity.getPlatform().toString());
                    dto.setUrl(streamerSocialLinkEntity.getUrl());
                    dto.setVisible(streamerSocialLinkEntity.getVisible());
                    return dto;
                }
        ).toList();

        streamerProfileResponse.setSocialLinks(streamerSocialLinkResponses);

        return streamerProfileResponse;
    }

    @Transactional
    public StreamerProfileResponse updateBio(
            Long streamerId,
            UpdateStreamerBioRequest request,
            MultipartFile avatar,
            MultipartFile thumb
    ) {

        StreamerEntity streamer = streamerRepository.findById(streamerId)
                .orElseThrow(() -> new RuntimeException("Streamer not found"));

        streamer.setDisplayName(request.getDisplayName());
        streamer.setBio(request.getBio());

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl =
                    fileUploadService.upload("STREAMER", avatar);

            streamer.setAvatar(avatarUrl);
        }

        if (thumb != null && !thumb.isEmpty()) {
            String thumbUrl =
                    fileUploadService.upload("THUMB", thumb);

            streamer.setThumb(thumbUrl);
        }

        streamerRepository.save(streamer);

        streamerSocialLinkRepository.deleteByStreamerId(streamerId);

        if (request.getSocialLinks() != null) {
            for (SocialLinkRequest link : request.getSocialLinks()) {

                if (link.getUrl() == null ||
                        link.getUrl().trim().isEmpty()) {
                    continue;
                }

                StreamerSocialLinkEntity entity =
                        new StreamerSocialLinkEntity();

                entity.setStreamerId(streamerId);
                entity.setPlatform(link.getPlatform());
                entity.setUrl(link.getUrl());
                entity.setVisible(link.getVisible());

                streamerSocialLinkRepository.save(entity);
            }
        }

        return getBio(streamerId);
    }
    public void updateFollowers(int cnt,StreamerEntity streamer){
        int currentFollowers = streamer.getFollowers();
        int afterFollowers = currentFollowers + cnt;

        streamer.setFollowers(afterFollowers);
        streamerRepository.save(streamer);
    }
}


