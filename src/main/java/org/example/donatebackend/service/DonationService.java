package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.DonationRequest;
import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.DonationResponse;
import org.example.donatebackend.dto.response.PaymentAccountResponse;
import org.example.donatebackend.dto.response.TopDonorResponse;
import org.example.donatebackend.dto.response.WalletResponse;
import org.example.donatebackend.entity.*;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.redis.RedisPublisher;
import org.example.donatebackend.repository.DonationRepository;
import org.example.donatebackend.repository.PaymentAccountRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private StreamerBlockService streamerBlockService;

    private String generateBankContent(Long streamerId) {
        return "BANK-DONATE-" +
                streamerId + "-" +
                System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    private String generateWalletContent(Long streamerId) {
        return "SYSTEM-DONATE-" +
                streamerId + "-" +
                System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateBlockedDonor(Long streamerId, Long donorId) {
        if (donorId == null) {
            return;
        }

        if (streamerBlockService.isBlocked(streamerId, donorId)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Bạn đã bị streamer chặn nên không thể donate"
            );
        }
    }

    private Donation buildDonation(DonationRequest req, String status, String content) {
        validateBlockedDonor(req.getStreamerId(), req.getDonorId());

        StreamerEntity streamer = streamerRepository.findById(req.getStreamerId())
                .orElseThrow(() -> new RuntimeException("Streamer not found"));

        Donation donation = new Donation();
        donation.setStreamer(streamer);
        donation.setDonorName(
                req.getDonorName() == null || req.getDonorName().isBlank()
                        ? "Anonymous"
                        : req.getDonorName().trim()
        );
        donation.setAmount(req.getAmount());
        donation.setMessage(req.getMessage());
        donation.setCreatedAt(LocalDateTime.now());
        donation.setDonorId(req.getDonorId());
        donation.setContent(content);
        donation.setStatus(status);

        return donation;
    }
    private Donation saveDonation(Donation donation) {
        return donationRepository.save(donation);
    }
    private List<TopDonorResponse> updateTopDonorRanking(String token, String donorName, Double amount) {
        String key = "ranking:streamer:" + token;

        redisTemplate.opsForZSet().incrementScore(key, donorName, amount);

        Set<ZSetOperations.TypedTuple<Object>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        if (result == null) {
            return List.of();
        }

        return result.stream().map(item -> {
            TopDonorResponse topDonorResponse = new TopDonorResponse();
            topDonorResponse.setDonorName((String) item.getValue());
            topDonorResponse.setTotalAmount(item.getScore());
            return topDonorResponse;
        }).toList();
    }

    private DonationResponse toDonationResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setDonorId(donation.getDonorId());
        response.setDonorName(donation.getDonorName());
        response.setAmount(donation.getAmount());
        response.setMessage(donation.getMessage());
        response.setStatus(donation.getStatus());
        response.setCreatedAt(donation.getCreatedAt());
        response.setContent(donation.getContent());
        response.setReferenceCode(donation.getReferenceCode());

        if (donation.getStreamer() != null) {
            response.setStreamerId(donation.getStreamer().getId());
            response.setStreamerName(donation.getStreamer().getDisplayName());
            response.setStreamerToken(donation.getStreamer().getToken());
            response.setStreamerAvatar(donation.getStreamer().getAvatar());
        }

        return response;
    }

    private DonationResponse buildDonationResponse(Donation savedDonation, List<TopDonorResponse> topDonors) {
        DonationResponse response = toDonationResponse(savedDonation);
        response.setTopDonors(topDonors);
        return response;
    }

    private Pageable safePageable(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return PageRequest.of(0, safeLimit);
    }

    private void sendDonationNotifications(Donation savedDonation) {
        StreamerEntity streamer = savedDonation.getStreamer();

        if (savedDonation.getDonorId() != null) {
            notificationService.createNotification(
                    savedDonation.getDonorId(),
                    NotificationEntity.NotificationType.DONATION,
                    "Donate thành công",
                    "Bạn đã donate " + savedDonation.getAmount() + "đ cho streamer " + streamer.getDisplayName(),
                    "/account/donations",
                    "{\"amount\":" + savedDonation.getAmount() + "}"
            );
        }

        if (streamer.getUserId() != null) {
            notificationService.createNotification(
                    streamer.getUserId(),
                    NotificationEntity.NotificationType.DONATION,
                    "Bạn vừa nhận được donate mới",
                    savedDonation.getDonorName() + " vừa donate " + savedDonation.getAmount() + "đ cho bạn",
                    "/account/donations",
                    "{\"amount\":" + savedDonation.getAmount() + "}"
            );
        }
    }

    public List<TopDonorResponse> findTopDonors(String token) {
        List<Object[]> objects = donationRepository.findTopDonors(token, PageRequest.of(0, 10));

        return objects.stream().map(o -> {
            TopDonorResponse donation = new TopDonorResponse();
            donation.setDonorName((String) o[0]);
            donation.setTotalAmount((Double) o[1]);
            return donation;
        }).toList();
    }

    public List<TopDonorResponse> findTopDonorsRedis(String token) {
        String key = "ranking:streamer:" + token;

        Set<ZSetOperations.TypedTuple<Object>> results =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, 0, 9);

        if (results == null || results.isEmpty()) {
            List<TopDonorResponse> topDonorResponses = findTopDonors(token);

            for (TopDonorResponse o : topDonorResponses) {
                String donorName = o.getDonorName();
                Double totalAmount = o.getTotalAmount();

                redisTemplate.opsForZSet().add(key, donorName, totalAmount);
            }

            results = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
        }

        if (results == null) {
            return List.of();
        }

        return results.stream().map(item -> {
            TopDonorResponse res = new TopDonorResponse();
            res.setDonorName((String) item.getValue());
            res.setTotalAmount(item.getScore());
            return res;
        }).toList();
    }

    public List<Donation> findTop10ByOrderByCreatedAtDesc() {
        return donationRepository.findTop10ByStatusOrderByCreatedAtDesc("SUCCESS");
    }

    public List<DonationResponse> getLatestDonations(Long streamerId, int limit) {
        Pageable pageable = safePageable(limit);

        List<Donation> donations =
                donationRepository.findByStreamer_IdAndStatusOrderByCreatedAtDesc(streamerId, "SUCCESS", pageable);

        return donations.stream()
                .map(this::toDonationResponse)
                .toList();
    }

    // USER thường: xem lịch sử mình đã donate cho streamer nào
    public List<DonationResponse> getMySentDonationHistory(String username, int limit) {
        UserEntity user = userService.findByUsername(username);

        List<Donation> donations =
                donationRepository.findByDonorIdAndStatusOrderByCreatedAtDesc(
                        user.getId(),
                        "SUCCESS",
                        safePageable(limit)
                );

        return donations.stream()
                .map(this::toDonationResponse)
                .toList();
    }

    // STREAMER: xem lịch sử donate mình đã nhận
    public List<DonationResponse> getMyReceivedDonationHistory(String username, int limit) {
        UserEntity user = userService.findByUsername(username);

        if (user.getRole() != UserEntity.Role.STREAMER) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Chỉ tài khoản STREAMER mới được xem lịch sử donate đã nhận"
            );
        }

        Long streamerId = streamerRepository.findByUserId(user.getId())
                .map(StreamerEntity::getId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Không tìm thấy streamer"
                ));

        java.util.Set<Long> blockedUserIds = streamerBlockService.getBlockedUserIds(streamerId);

        List<Donation> donations =
                donationRepository.findByStreamer_UserIdAndStatusOrderByCreatedAtDesc(
                        user.getId(),
                        "SUCCESS",
                        safePageable(limit)
                );

        return donations.stream()
                .map(donation -> {
                    DonationResponse response = toDonationResponse(donation);
                    response.setBlockedByStreamer(
                            donation.getDonorId() != null && blockedUserIds.contains(donation.getDonorId())
                    );
                    return response;
                })
                .toList();
    }

    // Endpoint /api/donate/history dùng chung cho menu Lịch Sử Donate
    public List<DonationResponse> getMyDonationHistory(String username, int limit) {
        UserEntity user = userService.findByUsername(username);

        if (user.getRole() == UserEntity.Role.STREAMER) {
            return getMyReceivedDonationHistory(username, limit);
        }

        return getMySentDonationHistory(username, limit);
    }

    private String buildQrUrl(Double amount, String content, String qrTemplate) {
        return qrTemplate
                + "?amount=" + amount
                + "&addInfo=" + content;
    }

    public PaymentAccountResponse createDonationBankQR(DonationRequest req, Long streamerId) {
        String content = generateBankContent(req.getStreamerId());

        Donation donation = saveDonation(buildDonation(req,"PENDING",content));

        PaymentAccountEntity paymentAccountEntity = paymentAccountRepository
                .findByStreamerId(streamerId)
                .orElse(new PaymentAccountEntity());

        PaymentAccountResponse res = new PaymentAccountResponse();

        String qrUrl = buildQrUrl(req.getAmount(), donation.getContent(), paymentAccountEntity.getQrTemplate());

        res.setDonationId(donation.getId().toString());
        res.setAmount(req.getAmount());
        res.setAddInfo(donation.getContent());
        res.setQrUrl(qrUrl);
        res.setStatus("PENDING");
        return res;
    }

    public Donation findByContentAndStatus(String content, String status) {
        return donationRepository.findByContentAndStatus(content, status);
    }

    public DonationResponse updateDonation(Donation donation) {
        Donation savedDonation = donationRepository.save(donation);

        List<TopDonorResponse> topDonorResponses = updateTopDonorRanking(
                savedDonation.getStreamer().getToken(),
                savedDonation.getDonorName(),
                savedDonation.getAmount()
        );

        DonationResponse response = buildDonationResponse(savedDonation, topDonorResponses);

        sendDonationNotifications(savedDonation);

        redisPublisher.publish(response);

        return response;
    }
    public DonationResponse createDonationByWallet(DonationRequest req){
        UserEntity user = userService.findByUserId(req.getDonorId());


        WalletEntity wallet =
                walletService.getOrCreateWallet(user);

        BigDecimal balanceBefore = wallet.getBalance();


        walletService.decreaseBalance(wallet, BigDecimal.valueOf(req.getAmount()));


        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setId(wallet.getId());
        walletResponse.setBalance(wallet.getBalance());
        walletResponse.setFrozenBalance(wallet.getFrozenBalance());
        walletResponse.setCurrency(wallet.getCurrency());
        walletResponse.setCreatedAt(wallet.getCreatedAt());
        walletResponse.setUserId(user.getId());


        BigDecimal balanceAfter =
                wallet.getBalance();
        walletTransactionService.createDonationOutTransaction(wallet, BigDecimal.valueOf(req.getAmount()), balanceBefore, balanceAfter,
                "DONATE_WALLET_" + System.currentTimeMillis()
        );


        Donation donation = buildDonation(req, "SUCCESS", null);
        DonationResponse donationResponse = updateDonation(donation);
        donationResponse.setWalletResponse(walletResponse);
        return donationResponse;
    }

    public PaymentAccountResponse createDonationQR(DonationRequest req,SystemPaymentMethod systemPaymentMethod, Long streamerId) {
        String content = generateWalletContent(streamerId);

        Donation donation = saveDonation(buildDonation(req,"PENDING",content));

        String qrUrl = buildQrUrl(req.getAmount(), donation.getContent(), systemPaymentMethod.getQrImageUrl());

        System.out.println(qrUrl);

        PaymentAccountResponse res = new PaymentAccountResponse();

        res.setDonationId(donation.getId().toString());
        res.setAmount(req.getAmount());
        res.setAddInfo(donation.getContent());
        res.setQrUrl(qrUrl);
        res.setStatus("PENDING");
        return res;
    }
}
