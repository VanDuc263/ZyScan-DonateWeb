package org.example.donatebackend.service;

import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.example.donatebackend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletTransactionService {
    @Autowired
    private WalletTransactionRepository
            walletTransactionRepository;
    @Autowired
    private WalletService walletService;

    public WalletTransactionEntity createWalletTransaction(
            UserEntity userEntity,
            String type,
            BigDecimal totalAmount,
            BigDecimal fee,
            BigDecimal netAmount,
            String transactionCode
    ) {

        WalletEntity walletEntity =
                walletService
                        .getOrCreateWallet(userEntity);

        WalletTransactionEntity tx =
                new WalletTransactionEntity();

        tx.setWallet(walletEntity);

        tx.setType(type);

        // tổng tiền user trả
        tx.setAmount(totalAmount);

        // phí hệ thống
        tx.setFee(fee);

        // tiền thật vào ví
        tx.setNetAmount(netAmount);

        tx.setTransactionCode(transactionCode);

        BigDecimal balance =
                walletEntity.getBalance();

        BigDecimal newBalance =
                balance.add(netAmount);

        tx.setBalanceBefore(balance);

        tx.setBalanceAfter(newBalance);

        tx.setStatus(
                TransactionStatus.PENDING
        );

        return walletTransactionRepository.save(tx);
    }
    public WalletTransactionEntity createDonationOutTransaction(
            WalletEntity wallet,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String transactionCode
    ) {

        WalletTransactionEntity tx =
                new WalletTransactionEntity();

        tx.setWallet(wallet);

        tx.setType("DONATION_OUT");

        // số tiền donate
        tx.setAmount(amount);

        // donate ví thường không fee
        tx.setFee(BigDecimal.ZERO);

        // số tiền thực bị trừ
        tx.setNetAmount(amount);

        tx.setTransactionCode(transactionCode);

        tx.setBalanceBefore(balanceBefore);

        tx.setBalanceAfter(balanceAfter);

        tx.setStatus(TransactionStatus.SUCCESS);

        return walletTransactionRepository.save(tx);
    }
    public WalletTransactionEntity
    findByTransactionCodeAndStatus(
            String transactionCode,
            TransactionStatus status
    ) {

        return walletTransactionRepository
                .findByTransactionCodeAndStatus(
                        transactionCode,
                        status
                );
    }

    public WalletTransactionEntity save(
            WalletTransactionEntity tx
    ) {

        return walletTransactionRepository.save(tx);
    }
}