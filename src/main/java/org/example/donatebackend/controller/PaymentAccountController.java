package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.PaymentAccountRequest;
import org.example.donatebackend.service.PaymentAccountService;
import org.example.donatebackend.service.StreamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-account")
public class PaymentAccountController {

    @Autowired
    private PaymentAccountService paymentAccountService;

    @Autowired
    private StreamerService streamerService;

    @PostMapping("/save")
    public ResponseEntity<?> savePaymentAccount(
            @RequestBody PaymentAccountRequest paymentAccountRequest,
            Authentication authentication
    ){
        return ResponseEntity.ok(paymentAccountService.savePaymentAccount(paymentAccountRequest,streamerService.getStreamerId(authentication.getName())));
    }

    @GetMapping("/bank-account")
    public ResponseEntity<?> getBankAccount(
            Authentication authentication
    ){
        return ResponseEntity.ok(paymentAccountService.getBankAccountByStreamerId(streamerService.getStreamerId(authentication.getName())));
    }

    @GetMapping("/qr")
    public ResponseEntity<?> getQrUrl(Authentication authentication){
        return ResponseEntity.ok(paymentAccountService.getQrUrlByStreamerId(streamerService.getStreamerId(authentication.getName())));
    }
}
