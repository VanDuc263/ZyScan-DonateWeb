package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.request.AdminUpdateReportStatusRequest;
import org.example.donatebackend.dto.response.ReportPenaltyResponse;
import org.example.donatebackend.dto.response.ViolationReportResponse;
import org.example.donatebackend.service.ViolationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Autowired
    private ViolationReportService violationReportService;

    @GetMapping
    public List<ViolationReportResponse> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType
    ) {
        return violationReportService.adminFindAll(status, targetType);
    }

    @GetMapping("/{id}")
    public ViolationReportResponse getReportById(@PathVariable Long id) {
        return violationReportService.adminFindById(id);
    }

    @PutMapping("/{id}/status")
    public ViolationReportResponse updateReportStatus(
            @PathVariable Long id,
            @RequestBody AdminUpdateReportStatusRequest req
    ) {
        return violationReportService.adminUpdateStatus(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteReport(@PathVariable Long id) {
        violationReportService.adminDelete(id);
        return Map.of("message", "Delete report successfully");
    }

    @GetMapping("/penalties/{userId}")
    public ReportPenaltyResponse getPenalty(@PathVariable Long userId) {
        return violationReportService.adminGetPenalty(userId);
    }
}
