package org.example.donatebackend.dto.request;

import org.example.donatebackend.entity.UserEntity;

public class AdminUpdateUserRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String avatar;
    private UserEntity.Role role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public UserEntity.Role getRole() { return role; }
    public void setRole(UserEntity.Role role) { this.role = role; }
}
