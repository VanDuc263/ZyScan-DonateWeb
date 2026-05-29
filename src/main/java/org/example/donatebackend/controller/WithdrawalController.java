package org.example.donatebackend.controller;


import org.example.donatebackend.dto.request.WithdrawRequest;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.service.UserService;
import org.example.donatebackend.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> withdrawMoneyFromWallet(
            Authentication authentication,
            @RequestBody WithdrawRequest  withdrawRequest
            ) {
        UserEntity user = userService.findByUsername(authentication.getName());

        BigDecimal amount = withdrawRequest.getAmount();

        return ResponseEntity.ok(withdrawalService.withdraw(user, amount));
    }

}
