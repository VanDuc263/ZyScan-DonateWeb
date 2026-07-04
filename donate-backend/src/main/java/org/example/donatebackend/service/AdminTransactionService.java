package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.AdminDonationResponse;
import org.example.donatebackend.dto.response.AdminPaymentResponse;
import org.example.donatebackend.dto.response.AdminWalletTransactionResponse;
import org.example.donatebackend.repository.DonationRepository;
import org.example.donatebackend.repository.PaymentRepository;
import org.example.donatebackend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminTransactionService {

    @Autowired private DonationRepository donationRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private AdminMapperService adminMapperService;

    @Transactional(readOnly = true)
    public List<AdminDonationResponse> findDonations(int limit) {
        return donationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit(limit)))
                .stream()
                .map(adminMapperService::toDonationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> findPayments(int limit) {
        return paymentRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit(limit)))
                .stream()
                .map(adminMapperService::toPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminWalletTransactionResponse> findWalletTransactions(int limit) {
        return walletTransactionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit(limit)))
                .stream()
                .map(adminMapperService::toWalletTransactionResponse)
                .toList();
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 200));
    }
}
