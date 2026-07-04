package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.request.AdminCreateUserRequest;
import org.example.donatebackend.dto.request.AdminUpdateUserRequest;
import org.example.donatebackend.dto.response.AdminUserResponse;
import org.example.donatebackend.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponse> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id);
    }

    @PostMapping
    public AdminUserResponse createUser(@RequestBody AdminCreateUserRequest req) {
        return adminUserService.createUser(req);
    }

    @PutMapping("/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id,
                                        @RequestBody AdminUpdateUserRequest req) {
        return adminUserService.updateUser(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Map.of("message", "Delete user successfully");
    }
}
