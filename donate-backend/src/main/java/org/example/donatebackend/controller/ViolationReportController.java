package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.CreateViolationReportRequest;
import org.example.donatebackend.dto.response.ViolationReportResponse;
import org.example.donatebackend.service.ViolationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ViolationReportController {

    @Autowired
    private ViolationReportService violationReportService;

    @PostMapping
    public ViolationReportResponse createReport(@RequestBody CreateViolationReportRequest req) {
        return violationReportService.createReport(req);
    }

    @GetMapping("/my")
    public List<ViolationReportResponse> getMyReports() {
        return violationReportService.getMyReports();
    }
}
