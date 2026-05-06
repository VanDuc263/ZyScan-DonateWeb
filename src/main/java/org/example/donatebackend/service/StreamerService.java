package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.StreamerRequest;
import org.example.donatebackend.dto.response.SearchStreamerResponse;
import org.example.donatebackend.dto.response.StreamerDetailResponse;
import org.example.donatebackend.dto.response.TopStreamerResponse;
import org.example.donatebackend.entity.NotificationEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.StreamerRepository;
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

    public StreamerDetailResponse getByDonateToken(String donateToken){
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
}


