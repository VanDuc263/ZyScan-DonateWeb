package org.example.donatebackend.service;

import jakarta.transaction.Transactional;
import org.example.donatebackend.dto.response.WithdrawResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.WalletRepository;
import org.example.donatebackend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WithdrawalService {


    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private WalletService walletService;

    @Transactional
    public WithdrawResponse withdraw(UserEntity user, BigDecimal amount){
        WalletEntity wallet = walletService.getOrCreateWallet(user);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Insufficient balance");
        }

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.subtract(amount);

        wallet.setBalance(after);
        wallet.setFrozenBalance(
                wallet.getFrozenBalance().add(amount)
        );



        walletService.save(wallet);

        String content = "WITHDRAW-" + user.getId() + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();



        WalletTransactionEntity transaction = walletTransactionService.createWalletTransaction(user,"WITHDRAW",amount,BigDecimal.ZERO,amount,content,before,after);
        return new WithdrawResponse(
                transaction.getTransactionCode(),
                amount,
                wallet.getBalance(),
                wallet.getFrozenBalance(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }
}
