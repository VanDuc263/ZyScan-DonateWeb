package org.example.donatebackend.controller;

import org.example.donatebackend.dto.request.SePayWebhookRequest;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.entity.WalletTransactionEntity;
import org.example.donatebackend.enums.TransactionStatus;
import org.example.donatebackend.service.DonationService;
import org.example.donatebackend.service.WalletService;
import org.example.donatebackend.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private  WalletTransactionService walletTransactionService;

    @Autowired
    private WalletService walletService;

    @PostMapping("/sepay")
    public Map<String, Boolean> webhook(
            @RequestBody SePayWebhookRequest req
    ) {

        // chỉ nhận tiền vào
        if (!"in".equalsIgnoreCase(
                req.getTransferType()
        )) {

            return Map.of("success", true);
        }

        String content = req.getContent();

        Long transferAmount =
                req.getTransferAmount();

        System.out.println(
                "Webhook content: " + content
        );

        /*
         * DONATION
         * VD:
         * DONATE-65-1778341742233
         */
        if (content.startsWith("DONATE-")) {

            Donation donation =
                    donationService
                            .findByContentAndStatus(
                                    content,
                                    "PENDING"
                            );

            if (donation != null) {

                donation.setStatus("SUCCESS");

                donation.setReferenceCode(
                        req.getReferenceCode()
                );

                donationService
                        .updateDonation(donation);

                System.out.println(
                        "Donation success"
                );
            }

            return Map.of("success", true);
        }

        /*
         * WALLET TOPUP
         * VD:
         * TOPUP-15-A8F2D1BC
         */
        if (content.startsWith("TOPUP-")) {

            WalletTransactionEntity tx =
                    walletTransactionService
                            .findByTransactionCodeAndStatus(
                                    content,
                                    TransactionStatus.PENDING
                            );

            if (tx != null) {

                if (tx.getStatus() ==
                        TransactionStatus.SUCCESS) {

                    return Map.of(
                            "success",
                            true
                    );
                }

                tx.setStatus(
                        TransactionStatus.SUCCESS
                );

                tx.setReferenceCode(
                        req.getReferenceCode()
                );

                walletTransactionService
                        .save(tx);

                // cộng tiền vào ví
                walletService.addBalance(
                        tx.getWallet(),
                        tx.getNetAmount()
                );

                System.out.println(
                        "Wallet topup success"
                );
            }

            return Map.of("success", true);
        }

        return Map.of("success", true);
    }
}