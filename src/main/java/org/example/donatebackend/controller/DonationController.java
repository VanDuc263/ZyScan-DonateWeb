package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.DonationRequest;
import org.example.donatebackend.dto.response.DonationResponse;
import org.example.donatebackend.dto.response.PaymentAccountResponse;
import org.example.donatebackend.dto.response.TopDonorResponse;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.service.DonationService;
import org.example.donatebackend.service.StreamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donate")
public class DonationController {
    @Autowired
    private DonationService donationService;

    @Autowired
    private StreamerService streamerService;

    @PostMapping("/qr")
    public ResponseEntity<PaymentAccountResponse> createDonationQR(
            @RequestBody DonationRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                donationService.createDonationQR(req, streamerService.getStreamerId(authentication.getName()))
        );
    }

    // Dùng chung cho trang /account/donations:
    // - USER: trả về lịch sử đã donate
    // - STREAMER: trả về lịch sử đã nhận donate
    @GetMapping("/history")
    public List<DonationResponse> getMyDonationHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return donationService.getMyDonationHistory(authentication.getName(), limit);
    }

    @GetMapping("/history/sent")
    public List<DonationResponse> getMySentDonationHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return donationService.getMySentDonationHistory(authentication.getName(), limit);
    }

    @GetMapping("/history/received")
    public List<DonationResponse> getMyReceivedDonationHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return donationService.getMyReceivedDonationHistory(authentication.getName(), limit);
    }

    @GetMapping("/{token}/top")
    public List<TopDonorResponse> getTopDonorByStreamer(@PathVariable String token) {
        return donationService.findTopDonorsRedis(token);
    }

    @GetMapping("/top")
    public List<Donation> getNewDonations() {
        return donationService.findTop10ByOrderByCreatedAtDesc();
    }

    @GetMapping("/{streamerId}/donations")
    public List<DonationResponse> getDonations(
            @PathVariable Long streamerId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return donationService.getLatestDonations(streamerId, limit);
    }
}
