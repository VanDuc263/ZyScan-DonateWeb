package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.response.AdminDonationResponse;
import org.example.donatebackend.dto.response.AdminPaymentResponse;
import org.example.donatebackend.dto.response.AdminWalletTransactionResponse;
import org.example.donatebackend.service.AdminTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {

    @Autowired
    private AdminTransactionService adminTransactionService;

    @GetMapping("/donations")
    public List<AdminDonationResponse> donations(@RequestParam(defaultValue = "100") int limit) {
        return adminTransactionService.findDonations(limit);
    }

    @GetMapping("/payments")
    public List<AdminPaymentResponse> payments(@RequestParam(defaultValue = "100") int limit) {
        return adminTransactionService.findPayments(limit);
    }

    @GetMapping("/wallets")
    public List<AdminWalletTransactionResponse> walletTransactions(@RequestParam(defaultValue = "100") int limit) {
        return adminTransactionService.findWalletTransactions(limit);
    }
}
