package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.WalletTransactionResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.example.donatebackend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletTransactionService {
    @Autowired
    private WalletTransactionRepository
            walletTransactionRepository;
    @Autowired
    private WalletService walletService;

    private boolean isCredit(String type){
        return type.equals("DEPOSIT") || type.equals("DONATION_IN") || type.equals("REFUND");
    }

    public WalletTransactionEntity createWalletTransaction(
            UserEntity userEntity,
            String type,
            BigDecimal totalAmount,
            BigDecimal fee,
            BigDecimal netAmount,
            String transactionCode,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter
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

        tx.setBalanceBefore(balanceBefore);

        tx.setBalanceAfter(balanceAfter);

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

    public Page<WalletTransactionResponse> getTransactions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return walletTransactionRepository
                .findByWallet_User_IdOrderByCreatedAtDesc(userId, pageable)
                .map(tx -> new WalletTransactionResponse(
                        tx.getId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getBalanceBefore(),
                        tx.getBalanceAfter(),
                        tx.getFee(),
                        tx.getNetAmount(),
                        tx.getReferenceType(),
                        tx.getReferenceId(),
                        tx.getStatus().name(),
                        tx.getCreatedAt(),
                        tx.getTransactionCode(),
                        tx.getReferenceCode()
                ));
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
