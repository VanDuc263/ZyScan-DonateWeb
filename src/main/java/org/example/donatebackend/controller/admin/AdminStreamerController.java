package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.request.AdminUpdateStreamerRequest;
import org.example.donatebackend.dto.response.AdminStreamerResponse;
import org.example.donatebackend.service.AdminStreamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/streamers")
public class AdminStreamerController {

    @Autowired
    private AdminStreamerService adminStreamerService;

    @GetMapping
    public List<AdminStreamerResponse> getAll() {
        return adminStreamerService.findAll();
    }

    @GetMapping("/{id}")
    public AdminStreamerResponse getById(@PathVariable Long id) {
        return adminStreamerService.findById(id);
    }

    @PutMapping("/{id}")
    public AdminStreamerResponse update(@PathVariable Long id, @RequestBody AdminUpdateStreamerRequest req) {
        return adminStreamerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        adminStreamerService.delete(id);
        return Map.of("message", "Delete streamer successfully");
    }
}
