package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.CreatePaymentQrRequest;
import org.example.donatebackend.dto.response.CreatePaymentQrResponse;
import org.example.donatebackend.dto.response.PaymentOrderStatusResponse;
import org.example.donatebackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/api/payments/create-qr")
    public ResponseEntity<CreatePaymentQrResponse> createQr(@RequestBody CreatePaymentQrRequest request) {
        return ResponseEntity.ok(paymentService.createQr(request));
    }

    @GetMapping("/api/payments/{orderCode}/status")
    public ResponseEntity<PaymentOrderStatusResponse> getStatus(@PathVariable String orderCode) {
        return ResponseEntity.ok(paymentService.getStatus(orderCode));
    }

    @PostMapping("/api/payments/{orderCode}/sandbox-simulate")
    public ResponseEntity<Map<String, String>> simulateSandboxPayment(@PathVariable String orderCode) {
        paymentService.simulateSandboxPayment(orderCode);
        return ResponseEntity.ok(Map.of("message", "Sandbox simulate requested"));
    }

    @PostMapping("/api/payments/vietqr/callback")
    public ResponseEntity<Map<String, String>> vietQrCallbackOld(@RequestBody Map<String, Object> payload) {
        paymentService.handleCallback(payload);
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }

    @PostMapping("/vqr/bank/api/transaction-sync")
    public ResponseEntity<Map<String, Object>> transactionSync(@RequestBody Map<String, Object> payload) {
        paymentService.handleCallback(payload);

        return ResponseEntity.ok(Map.of(
                "error", false,
                "errorReason", "",
                "toastMessage", "Transaction processed successfully",
                "object", Map.of("reftransactionid", "TXN_" + System.currentTimeMillis())
        ));
    }
}