package org.example.donatebackend.service;


import org.example.donatebackend.dto.request.PaymentAccountRequest;
import org.example.donatebackend.entity.PaymentAccountEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
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



        PaymentAccountEntity paymentAccountEntity = paymentAccountRepository.findByStreamerId(streamerId).orElse(new PaymentAccountEntity());

        paymentAccountEntity.setStreamerId(streamerId);
        paymentAccountEntity.setProviderType(paymentAccountRequest.getProviderType());
        paymentAccountEntity.setProviderCode(paymentAccountRequest.getProviderCode());
        paymentAccountEntity.setAccountName(paymentAccountRequest.getAccountName());
        paymentAccountEntity.setAccountNo(paymentAccountRequest.getAccountNo());

        String qrUrl = "https://img.vietqr.io/image/" + paymentAccountRequest.getProviderCode()
                + "-" +  paymentAccountRequest.getAccountNo() + "-compact1.png";

        paymentAccountEntity.setQrTemplate(qrUrl);
        paymentAccountEntity.setApiKey(paymentAccountRequest.getApiKey());
        paymentAccountEntity.setSecretKey(paymentAccountRequest.getSecretKey());

        return paymentAccountRepository.save(paymentAccountEntity);
    }

    public String getQrUrl(String name) {

        Long streamerId = streamerService.getStreamerId(name);

        PaymentAccountEntity paymentAccountEntity = paymentAccountRepository.findByStreamerId(streamerId).orElseThrow(
                () -> new AppException(ErrorCode.INTERNAL_ERROR,"Payment account not found")
        );
        return paymentAccountEntity.getQrTemplate();
    }
}
