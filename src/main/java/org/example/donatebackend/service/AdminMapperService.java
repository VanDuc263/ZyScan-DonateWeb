package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.*;
import org.example.donatebackend.entity.*;
import org.example.donatebackend.repository.DonationRepository;
import org.example.donatebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminMapperService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    public AdminDonationResponse toDonationResponse(Donation donation) {
        AdminDonationResponse res = new AdminDonationResponse();
        res.setId(donation.getId());
        res.setDonorId(donation.getDonorId());
        res.setDonorName(donation.getDonorName());
        res.setAmount(donation.getAmount());
        res.setMessage(donation.getMessage());
        res.setContent(donation.getContent());
        res.setReferenceCode(donation.getReferenceCode());
        res.setStatus(donation.getStatus());
        res.setCreatedAt(donation.getCreatedAt());

        if (donation.getStreamer() != null) {
            res.setStreamerId(donation.getStreamer().getId());
            res.setStreamerName(donation.getStreamer().getDisplayName());
            res.setStreamerToken(donation.getStreamer().getToken());
        }
        return res;
    }

    public AdminWalletTransactionResponse toWalletTransactionResponse(WalletTransactionEntity tx) {
        AdminWalletTransactionResponse res = new AdminWalletTransactionResponse();
        res.setId(tx.getId());
        res.setType(tx.getType());
        res.setAmount(tx.getAmount());
        res.setFee(tx.getFee());
        res.setNetAmount(tx.getNetAmount());
        res.setBalanceBefore(tx.getBalanceBefore());
        res.setBalanceAfter(tx.getBalanceAfter());
        res.setReferenceType(tx.getReferenceType());
        res.setReferenceId(tx.getReferenceId());
        res.setStatus(tx.getStatus());
        res.setTransactionCode(tx.getTransactionCode());
        res.setReferenceCode(tx.getReferenceCode());
        res.setCreatedAt(tx.getCreatedAt());

        if (tx.getWallet() != null) {
            res.setWalletId(tx.getWallet().getId());
            if (tx.getWallet().getUser() != null) {
                res.setUserId(tx.getWallet().getUser().getId());
                res.setUsername(tx.getWallet().getUser().getUsername());
            }
        }
        return res;
    }

    public AdminPaymentResponse toPaymentResponse(PaymentEntity payment) {
        AdminPaymentResponse res = new AdminPaymentResponse();
        res.setId(payment.getId());
        res.setDonationId(payment.getDonationId());
        res.setProvider(payment.getProvider());
        res.setTransactionCode(payment.getTransactionCode());
        res.setStatus(payment.getStatus());
        res.setCreatedAt(payment.getCreatedAt());
        res.setStreamerId(payment.getStreamerId());
        res.setDonorId(payment.getDonorId());
        res.setDonorName(payment.getDonorName());
        res.setAmount(payment.getAmount());
        res.setMessage(payment.getMessage());
        res.setBankCode(payment.getBankCode());
        res.setBankAccountNo(payment.getBankAccountNo());
        res.setBankAccountName(payment.getBankAccountName());
        res.setAddInfo(payment.getAddInfo());
        res.setQrUrl(payment.getQrUrl());
        res.setPaidAt(payment.getPaidAt());
        res.setDonationCreated(payment.getDonationCreated());
        return res;
    }

    public AdminStreamerResponse toStreamerResponse(StreamerEntity streamer) {
        AdminStreamerResponse res = new AdminStreamerResponse();
        res.setId(streamer.getId());
        res.setUserId(streamer.getUserId());
        res.setDisplayName(streamer.getDisplayName());
        res.setToken(streamer.getToken());
        res.setAvatar(streamer.getAvatar());
        res.setThumb(streamer.getThumb());
        res.setBio(streamer.getBio());
        res.setFollowers(streamer.getFollowers());
        res.setCreatedAt(streamer.getCreatedAt());
        res.setTotalReceived(donationRepository.sumSuccessAmountByStreamerId(streamer.getId()));
        res.setDonationCount(donationRepository.countByStreamer_IdAndStatus(streamer.getId(), "SUCCESS"));

        userRepository.findById(streamer.getUserId()).ifPresent(user -> {
            res.setUsername(user.getUsername());
            res.setEmail(user.getEmail());
        });
        return res;
    }

    public AdminPaymentMethodResponse toPaymentMethodResponse(SystemPaymentMethod method) {
        AdminPaymentMethodResponse res = new AdminPaymentMethodResponse();
        res.setId(method.getId());
        res.setProviderType(method.getProviderType());
        res.setBankCode(method.getBankCode());
        res.setAccountNumber(method.getAccountNumber());
        res.setAccountName(method.getAccountName());
        res.setQrTemplate(method.getQrTemplate());
        res.setQrImageUrl(method.getQrImageUrl());
        res.setActive(method.getActive());
        res.setCreatedAt(method.getCreatedAt());
        return res;
    }
}
