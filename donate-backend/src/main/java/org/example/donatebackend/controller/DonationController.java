package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.DonationRequest;
import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.DonationResponse;
import org.example.donatebackend.dto.response.PaymentAccountResponse;
import org.example.donatebackend.dto.response.TopDonorResponse;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.entity.SystemPaymentMethod;
import org.example.donatebackend.service.DonationService;
import org.example.donatebackend.service.StreamerService;
import org.example.donatebackend.service.SystemPaymentMethodService;
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

    @Autowired
    private SystemPaymentMethodService  systemPaymentMethodService;

    @PostMapping("/qr")
    public ResponseEntity<PaymentAccountResponse> createDonationQR(
            @RequestBody DonationRequest req,
            Authentication authentication
    ) {
        SystemPaymentMethod systemPaymentMethod = systemPaymentMethodService.getByMethodId(req.getMethodId());

        return ResponseEntity.ok(
                donationService.createDonationQR(req,systemPaymentMethod,req.getStreamerId())
        );
    }
    @PostMapping("/bank-qr")
    public ResponseEntity<PaymentAccountResponse> createDonationBankQR(
            @RequestBody DonationRequest req,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                donationService.createDonationBankQR(req,req.getStreamerId())
        );
    }

    @PostMapping("/wallet")
    public ResponseEntity<?> createDonationWallet(Authentication authentication, @RequestBody DonationRequest req) {
        return ResponseEntity.ok(donationService.createDonationByWallet(req));
    }
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
