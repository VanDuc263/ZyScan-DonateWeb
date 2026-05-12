package org.example.donatebackend.controller;


import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.PaymentQrResponse;
import org.example.donatebackend.service.SystemPaymentMethodService;
import org.example.donatebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private SystemPaymentMethodService service;

    @Autowired
    private UserService userService;

    @PostMapping("/generate-qr")
    public ResponseEntity<PaymentQrResponse> generateQrUrl(@RequestBody GenerateQrRequest req, Authentication authentication){
        String username = authentication.getName();

        Long userId = userService.findByUsername(username).getId();


        return ResponseEntity.ok(service.generateQr(req));
    }
}
