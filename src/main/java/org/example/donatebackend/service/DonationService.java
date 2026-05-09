package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.DonationRequest;
import org.example.donatebackend.dto.response.DonationResponse;
import org.example.donatebackend.dto.response.PaymentAccountResponse;
import org.example.donatebackend.dto.response.TopDonorResponse;
import org.example.donatebackend.entity.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    public Long saveDonationFromPaidOrder(PaymentOrderEntity order) {
        DonationRequest request = new DonationRequest();
        request.setStreamerId(order.getStreamerId());
        request.setDonorId(order.getDonorId());
        request.setDonorName(order.getDonorName());
        request.setAmount(order.getAmount());
        request.setMessage(order.getMessage());

        Donation savedDonation = createAndSaveDonation(request);

        List<TopDonorResponse> topDonorResponses = updateTopDonorRanking(
                savedDonation.getStreamer().getToken(),
                savedDonation.getDonorName(),
                savedDonation.getAmount()
        );

        DonationResponse response = buildDonationResponse(savedDonation, topDonorResponses);

        sendDonationNotifications(savedDonation);

        redisPublisher.publish(response);

        return savedDonation.getId();
    }

    private Donation createAndSaveDonation(DonationRequest req) {
        String uniqueContent = "DONATE-" + req.getStreamerId() + "-" + System.currentTimeMillis();

        StreamerEntity streamer = streamerRepository.findById(req.getStreamerId()).orElseThrow(
                () -> new RuntimeException("Streamer not found")
        );

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
        donation.setContent(uniqueContent);
        donation.setStatus("PENDING");

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

    private DonationResponse buildDonationResponse(Donation savedDonation, List<TopDonorResponse> topDonors) {
        DonationResponse response = new DonationResponse();
        response.setStreamerId(savedDonation.getStreamer().getId());
        response.setAmount(savedDonation.getAmount());
        response.setDonorName(savedDonation.getDonorName());
        response.setMessage(savedDonation.getMessage());
        response.setTopDonors(topDonors);
        return response;
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

            System.out.println("top donor" + topDonorResponses);

            for (TopDonorResponse o : topDonorResponses) {
                String donorName = o.getDonorName();
                Double totalAmount = o.getTotalAmount();

                redisTemplate.opsForZSet().add(key, donorName, totalAmount);
            }

            results = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
            System.out.println("results: " + results);
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
        Pageable pageable = PageRequest.of(0, limit);

        List<Donation> donations =
                donationRepository.findByStreamer_IdAndStatusOrderByCreatedAtDesc(streamerId,"SUCCESS", pageable);

        return donations.stream().map(d -> {
            DonationResponse res = new DonationResponse();
            res.setStreamerId(streamerId);
            res.setAmount(d.getAmount());
            res.setDonorName(d.getDonorName());
            res.setMessage(d.getMessage());
            return res;
        }).toList();
    }
    private String buildQrUrl(Double amount, String content, String qrTemplate) {

        String qrUrl = qrTemplate
                + "?amount=" + amount
                + "&addInfo=" + content;

        return qrUrl;
    }
    public PaymentAccountResponse createDonationQR(DonationRequest req, Long streamerId) {
        Donation donation= createAndSaveDonation(req);

        PaymentAccountEntity paymentAccountEntity = paymentAccountRepository.findByStreamerId(streamerId).orElse(new PaymentAccountEntity());

        PaymentAccountResponse res = new PaymentAccountResponse();

        String qrUrl = buildQrUrl(req.getAmount(),donation.getContent(),paymentAccountEntity.getQrTemplate());

        res.setOrderCode(donation.getId().toString());
        res.setAmount(req.getAmount());
        res.setAddInfo(donation.getContent());
        res.setQrUrl(qrUrl);
        res.setStatus("PENDING");
        return res;
    }
    public Donation findByContentAndStatus(String content,String status){
        return donationRepository.findByContentAndStatus(content,status);
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
}