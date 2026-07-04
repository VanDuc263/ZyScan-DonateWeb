package org.example.donatebackend.mapper;

import org.example.donatebackend.dto.response.WalletResponse;
import org.example.donatebackend.entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(
            WalletEntity wallet
    ) {

        WalletResponse response =
                new WalletResponse();

        response.setId(wallet.getId());

        response.setUserId(
                wallet.getUser().getId()
        );

        response.setBalance(
                wallet.getBalance()
        );

        response.setFrozenBalance(
                wallet.getFrozenBalance()
        );

        response.setCurrency(
                wallet.getCurrency()
        );

        response.setCreatedAt(
                wallet.getCreatedAt()
        );

        return response;
    }
}