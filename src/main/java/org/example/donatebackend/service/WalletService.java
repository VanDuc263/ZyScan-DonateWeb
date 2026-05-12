package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.WalletResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public WalletResponse getWalletByUser(UserEntity userEntity) {
        WalletEntity wallet = walletRepository.findByUser(userEntity).orElseGet(
                () -> {
                    WalletEntity newWallet = new WalletEntity();
                    newWallet.setUser(userEntity);
                    newWallet.setBalance(BigDecimal.valueOf(0));
                    newWallet.setFrozenBalance(BigDecimal.valueOf(0));
                    newWallet.setCurrency("VND");
                    walletRepository.save(newWallet);
                    return newWallet;
                }
        );

        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setId(wallet.getId());
        walletResponse.setUserId(wallet.getUser().getId());
        walletResponse.setBalance(wallet.getBalance());
        walletResponse.setFrozenBalance(wallet.getFrozenBalance());
        walletResponse.setCurrency(wallet.getCurrency());
        walletResponse.setCreatedAt(wallet.getCreatedAt());

        return walletResponse;
    }
}
