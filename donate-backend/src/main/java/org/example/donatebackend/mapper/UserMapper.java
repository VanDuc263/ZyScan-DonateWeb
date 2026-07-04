package org.example.donatebackend.mapper;

import org.example.donatebackend.dto.response.UserResponse;
import org.example.donatebackend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toUserResponse(UserEntity user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setRole(user.getRole());
        res.setAvatar(user.getAvatar());
        return res;
    }
}
