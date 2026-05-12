package org.example.donatebackend.controller;


import org.checkerframework.checker.units.qual.A;
import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.PaymentQrResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.service.SystemPaymentMethodService;
import org.example.donatebackend.service.UserService;
import org.example.donatebackend.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private SystemPaymentMethodService service;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @PostMapping("/generate-qr")
    public ResponseEntity<PaymentQrResponse> generateQrUrl(
            @RequestBody GenerateQrRequest req,
            Authentication authentication
    ) {

        String username = authentication.getName();

        UserEntity user =
                userService.findByUsername(username);

        return ResponseEntity.ok(
                service.generateQr(req, user)
        );
    }
}
