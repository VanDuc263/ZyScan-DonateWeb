package org.example.donatebackend.controller;


import org.example.donatebackend.dto.request.SePayWebhookRequest;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private DonationService donationService;

    @PostMapping("/sepay")
    public Map<String, Boolean> webhook(@RequestBody SePayWebhookRequest req) {

        if (!"in".equals(req.getTransferType())) {
            return Map.of("success", true);
        }

        String content = req.getContent();
        Long amount = req.getTransferAmount();

        Donation donation = donationService
                .findByContentAndStatus(content, "PENDING");

        System.out.println(content);
        System.out.println(donation);

        if (donation != null) {

            donation.setStatus("SUCCESS");
            donation.setReferenceCode(req.getReferenceCode());

            donationService.updateDonation(donation);

        }

        return Map.of("success", true);
    }
}
