package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.UpdateProfileRequest;
import org.example.donatebackend.entity.NotificationEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private NotificationService notificationService;

    public UserEntity updateAvatar(String username,String url){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("user not found")
        );

        user.setAvatar(url);

        return userRepository.save(user);

    }

    public String uploadUserAvatar(String username, MultipartFile file) {
        String url = fileUploadService.upload("USER", file);
        UserEntity updatedUser = updateAvatar(username, url);

        notificationService.createNotification(
                updatedUser.getId(),
                NotificationEntity.NotificationType.ACCOUNT,
                "Cập nhật avatar thành công",
                "Ảnh đại diện của bạn đã được thay đổi",
                "/account/profile",
                null
        );

        return url;
    }
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND,"user not found")
        );
    }
    public UserEntity updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim());
        }

        UserEntity savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser.getId(),
                NotificationEntity.NotificationType.ACCOUNT,
                "Cập nhật thông tin thành công",
                "Thông tin tài khoản của bạn đã được cập nhật",
                "/account/profile",
                null
        );

        return savedUser;
    }
}
