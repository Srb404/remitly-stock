package com.remitly.exchange.service;

import com.remitly.exchange.domain.WalletStock;
import com.remitly.exchange.domain.WalletStockId;
import com.remitly.exchange.repository.WalletStockRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletStockRepository walletStockRepository;

    public WalletService(WalletStockRepository walletStockRepository) {
        this.walletStockRepository = walletStockRepository;
    }

    @Transactional(readOnly = true)
    public List<WalletStock> listByWallet(String walletId) {
        return walletStockRepository.findByWalletId(walletId);
    }

    @Transactional(readOnly = true)
    public Optional<WalletStock> findOne(String walletId, String stockName) {
        return walletStockRepository.findById(new WalletStockId(walletId, stockName));
    }
}