package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.CreatePaymentQrRequest;
import org.example.donatebackend.dto.response.CreatePaymentQrResponse;
import org.example.donatebackend.dto.response.PaymentOrderStatusResponse;
import org.example.donatebackend.entity.PaymentOrderEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.repository.PaymentOrderRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
public class PaymentService {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private DonationService donationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${vietqr.username}")
    private String vietqrUsername;

    @Value("${vietqr.password}")
    private String vietqrPassword;

    @Value("${vietqr.bank-code}")
    private String vietqrBankCode;

    @Value("${vietqr.bank-account}")
    private String vietqrBankAccount;

    @Value("${vietqr.bank-account-name}")
    private String vietqrBankAccountName;

    public CreatePaymentQrResponse createQr(CreatePaymentQrRequest request) {
        StreamerEntity streamer = streamerRepository.findById(request.getStreamerId())
                .orElseThrow(() -> new RuntimeException("Streamer not found"));

        PaymentOrderEntity order = new PaymentOrderEntity();
        order.setTransactionCode(generateOrderCode());
        order.setStreamerId(streamer.getId());
        order.setDonorId(request.getDonorId());
        order.setDonorName(
                request.getDonorName() == null || request.getDonorName().isBlank()
                        ? "Anonymous"
                        : request.getDonorName().trim()
        );
        order.setAmount(request.getAmount());
        order.setMessage(request.getMessage());
        order.setProvider("VIETQR");
        order.setBankCode(vietqrBankCode);
        order.setBankAccountNo(vietqrBankAccount);
        order.setBankAccountName(vietqrBankAccountName);
        order.setAddInfo(order.getTransactionCode());
        order.setStatus(PaymentOrderEntity.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setDonationCreated(false);

        try {
            String accessToken = getAccessToken();
            Map<String, Object> generateResponse = callGenerateQr(accessToken, order);
            String qrUrl = extractQrUrl(generateResponse);
            order.setQrUrl(qrUrl);
        } catch (Exception e) {
            order.setQrUrl("");
        }
        paymentOrderRepository.save(order);



        CreatePaymentQrResponse response = new CreatePaymentQrResponse();
        response.setOrderCode(order.getTransactionCode());
        response.setBankCode(order.getBankCode());
        response.setAccountNo(order.getBankAccountNo());
        response.setAccountName(order.getBankAccountName());
        response.setAmount(order.getAmount());
        response.setAddInfo(order.getAddInfo());
        response.setQrUrl(order.getQrUrl());
        response.setStatus(order.getStatus().name());
        return response;
    }

    public PaymentOrderStatusResponse getStatus(String orderCode) {
        PaymentOrderEntity order = paymentOrderRepository.findByTransactionCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));

        PaymentOrderStatusResponse response = new PaymentOrderStatusResponse();
        response.setOrderCode(order.getTransactionCode());
        response.setStatus(order.getStatus().name());
        response.setAmount(order.getAmount());
        response.setDonationCreated(order.getDonationCreated());
        return response;
    }

    public void simulateSandboxPayment(String orderCode) {
        PaymentOrderEntity order = paymentOrderRepository.findByTransactionCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));

        if (order.getStatus() == PaymentOrderEntity.Status.PAID) {
            return;
        }

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> body = Map.of(
                "bankAccount", order.getBankAccountNo(),
                "content", order.getAddInfo(),
                "amount", order.getAmount().longValue(),
                "bankCode", order.getBankCode(),
                "transType", "C"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                "https://dev.vietqr.org/vqr/bank/api/test/transaction-callback",
                HttpMethod.POST,
                entity,
                Map.class
        );
    }

    @Transactional
    public void handleCallback(Map<String, Object> payload) {
        String content = extractAnyString(payload, "content", "addInfo", "description");
        Double amount = extractAnyDouble(payload, "amount", "creditAmount");

        if (content == null) {
            return;
        }

        PaymentOrderEntity order = paymentOrderRepository.findByAddInfo(content)
                .orElse(null);

        if (order == null) {
            return;
        }

        if (amount != null && !Objects.equals(order.getAmount().longValue(), amount.longValue())) {
            return;
        }

        if (order.getStatus() == PaymentOrderEntity.Status.PAID && Boolean.TRUE.equals(order.getDonationCreated())) {
            return;
        }

        order.setStatus(PaymentOrderEntity.Status.PAID);
        order.setPaidAt(LocalDateTime.now());
        paymentOrderRepository.save(order);

        if (!Boolean.TRUE.equals(order.getDonationCreated())) {
            Long donationId = donationService.saveDonationFromPaidOrder(order);
            order.setDonationId(donationId);
            order.setDonationCreated(true);
            paymentOrderRepository.save(order);
        }
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String basic = Base64.getEncoder().encodeToString(
                (vietqrUsername + ":" + vietqrPassword).getBytes(StandardCharsets.UTF_8)
        );
        headers.set("Authorization", "Basic " + basic);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://dev.vietqr.org/vqr/api/token_generate",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new RuntimeException("Không lấy được access token VietQR");
        }

        return body.get("access_token").toString();
    }

    private Map<String, Object> callGenerateQr(String accessToken, PaymentOrderEntity order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> body = Map.of(
                "bankCode", order.getBankCode(),
                "bankAccount", order.getBankAccountNo(),
                "userBankName", order.getBankAccountName(),
                "amount", order.getAmount().longValue(),
                "content", order.getAddInfo(),
                "qrType", 0,
                "note", order.getMessage() == null ? "" : order.getMessage()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://dev.vietqr.org/vqr/api/qr/generate-customer",
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Không tạo được QR VietQR");
        }

        return response.getBody();
    }

    private String extractQrUrl(Map<String, Object> response) {
        String direct = pickString(response, "qrDataURL", "qrDataUrl", "qrUrl", "qrCode", "qrImage", "image");
        if (direct != null) {
            return direct;
        }

        Object data = response.get("data");
        if (data instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) nested;
            String nestedValue = pickString(nestedMap, "qrDataURL", "qrDataUrl", "qrUrl", "qrCode", "qrImage", "image");
            if (nestedValue != null) {
                return nestedValue;
            }
        }

        throw new RuntimeException("Không đọc được qrUrl từ response VietQR");
    }

    private String pickString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private String extractAnyString(Map<String, Object> payload, String... keys) {
        String direct = pickString(payload, keys);
        if (direct != null) {
            return direct;
        }

        Object data = payload.get("data");
        if (data instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) nested;
            return pickString(nestedMap, keys);
        }

        return null;
    }

    private Double extractAnyDouble(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            Double parsed = parseDouble(value);
            if (parsed != null) {
                return parsed;
            }
        }

        Object data = payload.get("data");
        if (data instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) nested;
            for (String key : keys) {
                Double parsed = parseDouble(nestedMap.get(key));
                if (parsed != null) {
                    return parsed;
                }
            }
        }

        return null;
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String generateOrderCode() {
        String seed = String.valueOf(System.currentTimeMillis());
        String suffix = String.format("%04d", random.nextInt(10000));
        String code = "DN" + seed.substring(Math.max(0, seed.length() - 10)) + suffix;
        return code.length() > 23 ? code.substring(0, 23) : code;
    }
}