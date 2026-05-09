package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.DonationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendDonateAlert(Long streamerId, DonationResponse donation){
        simpMessagingTemplate.convertAndSend(
                "/topic/donate/" + streamerId,
                donation
        );
    }
//    public void sendPaymentSuccess(Long streamerId, PaymentEvent event) {
//        messagingTemplate.convertAndSend(
//                "/topic/payment/" + streamerId,
//                event
//        );
//    }
}
