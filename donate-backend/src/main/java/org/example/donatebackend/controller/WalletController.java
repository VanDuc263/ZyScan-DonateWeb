package org.example.donatebackend.controller;

import org.example.donatebackend.dto.response.WalletResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.service.UserService;
import org.example.donatebackend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet(Authentication authentication) {
        String username = authentication.getName();

        UserEntity user = userService.findByUsername(username);

        return ResponseEntity.ok(walletService.getWalletResponse(user));

    }
}
