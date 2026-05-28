package org.example.donatebackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponse(
        Long id,
        String type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        BigDecimal fee,
        BigDecimal netAmount,
        String referenceType,
        Long referenceId,
        String status,
        LocalDateTime createdAt,
        String transactionCode,
        String referenceCode
) {}
