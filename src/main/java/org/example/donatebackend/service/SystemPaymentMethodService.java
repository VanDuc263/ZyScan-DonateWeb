package org.example.donatebackend.service;


import org.example.donatebackend.dto.request.GenerateQrRequest;
import org.example.donatebackend.dto.response.PaymentQrResponse;
import org.example.donatebackend.entity.SystemPaymentMethod;
import org.example.donatebackend.repository.SystemPaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemPaymentMethodService {

    @Autowired
    private SystemPaymentMethodRepository systemPaymentMethodRepository;

    public PaymentQrResponse generateQr(GenerateQrRequest req) {

        SystemPaymentMethod method = systemPaymentMethodRepository.findById(req.getMethodId())
                .orElseThrow(() -> new RuntimeException("Method not found"));

        String qrUrl = buildQrUrl(method, req.getAmount(),"ni");

        PaymentQrResponse res = new PaymentQrResponse();
        res.setQrUrl(qrUrl);
        res.setAmount(req.getAmount());

        return res;
    }

    private String buildQrUrl(SystemPaymentMethod method, Double amount, String content) {
        return method.getQrImageUrl()
                + "?amount=" + amount
                + "&addInfo=" + content
                + "&account=" + method.getAccountNumber();
    }
}
