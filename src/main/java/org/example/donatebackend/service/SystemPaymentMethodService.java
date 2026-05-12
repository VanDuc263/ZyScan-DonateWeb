package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.PaymentQrResponse;
import org.example.donatebackend.entity.SystemPaymentMethod;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.repository.SystemPaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class SystemPaymentMethodService {

    @Autowired
    private SystemPaymentMethodRepository repository;

    @Autowired
    private WalletTransactionService
            walletTransactionService;

    public PaymentQrResponse generateQr(
            GenerateQrRequest req,
            UserEntity user
    ) {

        SystemPaymentMethod method =
                repository.findById(req.getMethodId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Method not found"
                                )
                        );

        // tiền user muốn nạp
        BigDecimal amount =
                BigDecimal.valueOf(
                        req.getAmount()
                );

        // phí
        BigDecimal fee =
                amount.multiply(
                        BigDecimal.valueOf(0.01)
                ).setScale(
                        0,
                        RoundingMode.HALF_UP
                );

        // tổng tiền cần chuyển
        BigDecimal totalAmount =
                amount.add(fee);

        String content =
                "TOPUP-"
                        + user.getId()
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        // SAVE TRANSACTION
        walletTransactionService
                .createWalletTransaction(
                        user,
                        "DEPOSIT",
                        totalAmount,
                        fee,
                        amount,
                        content
                );

        String qrUrl = buildQrUrl(
                method,
                totalAmount,
                content
        );

        PaymentQrResponse res =
                new PaymentQrResponse();

        res.setQrUrl(qrUrl);

        // tiền user nhập
        res.setAmount(
                amount.doubleValue()
        );

//        res.setFee(
//                fee.doubleValue()
//        );
//
//        res.setTotalAmount(
//                totalAmount.doubleValue()
//        );

        res.setContent(content);

        return res;
    }

    private String buildQrUrl(
            SystemPaymentMethod method,
            BigDecimal amount,
            String content
    ) {

        return method.getQrImageUrl()
                + "?amount=" + amount
                + "&addInfo=" + content
                + "&account=" +
                method.getAccountNumber();
    }
}