package org.example.donatebackend.service;


import org.example.donatebackend.dto.request.PaymentAccountRequest;
import org.example.donatebackend.entity.PaymentAccountEntity;
import org.example.donatebackend.repository.PaymentAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentAccountService {


    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private StreamerService streamerService;


    public PaymentAccountEntity savePaymentAccount(PaymentAccountRequest paymentAccountRequest,String username){
        Long streamerId = streamerService.getStreamerId(username);

        PaymentAccountEntity paymentAccountEntity = new PaymentAccountEntity();
        paymentAccountEntity.setStreamerId(streamerId);
        paymentAccountEntity.setProviderType(paymentAccountRequest.getProviderType());
        paymentAccountEntity.setProviderCode(paymentAccountRequest.getProviderCode());
        paymentAccountEntity.setAccountName(paymentAccountRequest.getAccountName());
        paymentAccountEntity.setAccountNo(paymentAccountRequest.getAccountNo());
        paymentAccountEntity.setApiKey(paymentAccountRequest.getApiKey());
        paymentAccountEntity.setSecretKey(paymentAccountRequest.getSecretKey());
        paymentAccountEntity.setQrTemplate(paymentAccountRequest.getQrTemplate());

        return paymentAccountRepository.save(paymentAccountEntity);
    }
}
