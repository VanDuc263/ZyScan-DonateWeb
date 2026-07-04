package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.request.PaymentMethodRequest;
import org.example.donatebackend.dto.response.AdminPaymentMethodResponse;
import org.example.donatebackend.dto.response.PaymentQrResponse;
import org.example.donatebackend.entity.SystemPaymentMethod;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.repository.SystemPaymentMethodRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.example.donatebackend.config.CacheConfig.SYSTEM_PAYMENT_METHOD_BY_ID_CACHE;

@Service
public class SystemPaymentMethodService {

    @Autowired
    private SystemPaymentMethodRepository repository;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private AdminMapperService adminMapperService;

    @Autowired
    private WalletService walletService;

    public PaymentQrResponse generateQr(GenerateQrRequest req, UserEntity user) {
        SystemPaymentMethod method = getByMethodId(req.getMethodId());

        BigDecimal amount = BigDecimal.valueOf(req.getAmount());
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.01)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalAmount = amount.add(fee);

        String content = "TOPUP-" + user.getId() + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        WalletEntity wallet = walletService.getOrCreateWallet(user);

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(amount);

        walletTransactionService.createWalletTransaction(user, "DEPOSIT", totalAmount, fee, amount, content,before,after);

        String qrUrl = buildQrUrl(method, totalAmount, content);

        PaymentQrResponse res = new PaymentQrResponse();
        res.setQrUrl(qrUrl);
        res.setAmount(amount.doubleValue());
        res.setContent(content);
        return res;
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentMethodResponse> adminFindAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(adminMapperService::toPaymentMethodResponse)
                .toList();
    }

    @Transactional
    public AdminPaymentMethodResponse adminCreate(PaymentMethodRequest req) {
        SystemPaymentMethod method = new SystemPaymentMethod();
        applyRequest(method, req, true);
        return adminMapperService.toPaymentMethodResponse(repository.save(method));
    }

    @Transactional
    @CacheEvict(cacheNames = SYSTEM_PAYMENT_METHOD_BY_ID_CACHE, key = "#id")
    public AdminPaymentMethodResponse adminUpdate(Long id, PaymentMethodRequest req) {
        SystemPaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        applyRequest(method, req, false);
        return adminMapperService.toPaymentMethodResponse(repository.save(method));
    }

    @Transactional
    @CacheEvict(cacheNames = SYSTEM_PAYMENT_METHOD_BY_ID_CACHE, key = "#id")
    public void adminDelete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Payment method not found");
        }
        repository.deleteById(id);
    }

    private void applyRequest(SystemPaymentMethod method, PaymentMethodRequest req, boolean creating) {
        if (creating && (req.getProviderType() == null || req.getProviderType().trim().isEmpty())) {
            throw new RuntimeException("providerType is required");
        }
        if (req.getProviderType() != null) method.setProviderType(trimOrNull(req.getProviderType()));
        if (req.getBankCode() != null) method.setBankCode(trimOrNull(req.getBankCode()));
        if (req.getAccountNumber() != null) method.setAccountNumber(trimOrNull(req.getAccountNumber()));
        if (req.getAccountName() != null) method.setAccountName(trimOrNull(req.getAccountName()));
        if (req.getQrTemplate() != null) method.setQrTemplate(trimOrNull(req.getQrTemplate()));
        if (req.getQrImageUrl() != null) method.setQrImageUrl(trimOrNull(req.getQrImageUrl()));
        if (req.getActive() != null) method.setActive(req.getActive());
        if (method.getActive() == null) method.setActive(true);
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildQrUrl(SystemPaymentMethod method, BigDecimal amount, String content) {
        return method.getQrImageUrl()
                + "?amount=" + amount
                + "&addInfo=" + content
                + "&account=" + method.getAccountNumber();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SYSTEM_PAYMENT_METHOD_BY_ID_CACHE, key = "#methodId")
    public SystemPaymentMethod getByMethodId(Long methodId) {
        return repository.findById(methodId).orElseThrow(() -> new RuntimeException("Method not found"));
    }
}
