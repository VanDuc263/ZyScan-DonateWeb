package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.UpdateProfileRequest;
import org.example.donatebackend.dto.response.UserResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.mapper.UserMapper;
import org.example.donatebackend.service.FileUploadService;
import org.example.donatebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.uploadUserAvatar(username,file);
    }
    @PutMapping("/profile")
    public UserResponse updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserEntity updatedUser = userService.updateProfile(username, request);
        return userMapper.toUserResponse(updatedUser);
    }
}
