package org.example.donatebackend.controller;


import org.example.donatebackend.service.UserService;
import org.example.donatebackend.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet-transactions")
public class WalletTransactionController {

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getWalletTransactions(Authentication authentication) {
        String username = authentication.getName();

        Long userId = userService.findByUsername(username).getId();

        return ResponseEntity.ok(walletTransactionService.getTransactions(userId,0,10));
    }
}
