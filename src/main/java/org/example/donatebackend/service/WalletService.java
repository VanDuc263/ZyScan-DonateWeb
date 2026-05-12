package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.WalletResponse;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.WalletEntity;
import org.example.donatebackend.mapper.WalletMapper;
import org.example.donatebackend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletMapper walletMapper;

    public WalletEntity getOrCreateWallet(
            UserEntity user
    ) {

        return walletRepository.findByUser(user)
                .orElseGet(() -> {

                    WalletEntity wallet =
                            new WalletEntity();

                    wallet.setUser(user);

                    wallet.setBalance(BigDecimal.ZERO);

                    wallet.setFrozenBalance(
                            BigDecimal.ZERO
                    );

                    wallet.setCurrency("VND");

                    return walletRepository.save(wallet);
                });
    }

    public WalletResponse getWalletResponse(
            UserEntity user
    ) {

        WalletEntity wallet =
                getOrCreateWallet(user);

        return walletMapper.toResponse(wallet);
    }

    public void addBalance(WalletEntity wallet, BigDecimal bigDecimal) {
        wallet.setBalance(wallet.getBalance().add(bigDecimal));
        walletRepository.save(wallet);
    }
}