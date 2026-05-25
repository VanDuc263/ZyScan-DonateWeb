package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.response.AdminStatsResponse;
import org.example.donatebackend.service.AdminOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    @Autowired
    private AdminOverviewService adminOverviewService;

    @GetMapping("/overview")
    public AdminStatsResponse overview() {
        return adminOverviewService.getStats();
    }
}
