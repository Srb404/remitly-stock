package com.remitly.exchange.service;

import com.remitly.exchange.domain.AuditLogEntry;
import com.remitly.exchange.domain.BankStock;
import com.remitly.exchange.domain.OperationType;
import com.remitly.exchange.domain.WalletStock;
import com.remitly.exchange.dto.WalletStockDto;
import com.remitly.exchange.exception.InsufficientStockException;
import com.remitly.exchange.exception.StockNotFoundException;
import com.remitly.exchange.repository.AuditLogRepository;
import com.remitly.exchange.repository.BankStockRepository;
import com.remitly.exchange.repository.WalletStockRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradingService {

    private static final Logger log = LoggerFactory.getLogger(TradingService.class);

    private final BankStockRepository bankStockRepository;
    private final WalletStockRepository walletStockRepository;
    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public TradingService(
            BankStockRepository bankStockRepository,
            WalletStockRepository walletStockRepository,
            AuditLogRepository auditLogRepository,
            Clock clock) {
        this.bankStockRepository = bankStockRepository;
        this.walletStockRepository = walletStockRepository;
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    @Transactional
    public WalletStockDto buy(String walletId, String stockName) {
        BankStock bank = bankStockRepository.findForUpdate(stockName)
                .orElseThrow(() -> new StockNotFoundException(stockName));
        if (bank.getQuantity() <= 0) {
            throw new InsufficientStockException(
                    "Bank has no units of stock: " + stockName);
        }
        bank.setQuantity(bank.getQuantity() - 1);

        WalletStock wallet = walletStockRepository.findForUpdate(walletId, stockName)
                .orElseGet(() -> walletStockRepository.save(
                        new WalletStock(walletId, stockName, 0)));
        wallet.setQuantity(wallet.getQuantity() + 1);

        auditLogRepository.save(new AuditLogEntry(
                OperationType.BUY, walletId, stockName, OffsetDateTime.now(clock)));

        log.info("trade executed: type={} wallet={} stock={} walletQty={} bankQty={}",
                OperationType.BUY, walletId, stockName, wallet.getQuantity(), bank.getQuantity());
        return WalletStockDto.from(wallet);
    }

    @Transactional
    public WalletStockDto sell(String walletId, String stockName) {
        BankStock bank = bankStockRepository.findForUpdate(stockName)
                .orElseThrow(() -> new StockNotFoundException(stockName));

        WalletStock wallet = walletStockRepository.findForUpdate(walletId, stockName)
                .orElseThrow(() -> new InsufficientStockException(
                        "Wallet " + walletId + " has no units of stock: " + stockName));
        if (wallet.getQuantity() <= 0) {
            throw new InsufficientStockException(
                    "Wallet " + walletId + " has no units of stock: " + stockName);
        }
        wallet.setQuantity(wallet.getQuantity() - 1);
        bank.setQuantity(bank.getQuantity() + 1);

        auditLogRepository.save(new AuditLogEntry(
                OperationType.SELL, walletId, stockName, OffsetDateTime.now(clock)));

        log.info("trade executed: type={} wallet={} stock={} walletQty={} bankQty={}",
                OperationType.SELL, walletId, stockName, wallet.getQuantity(), bank.getQuantity());
        return WalletStockDto.from(wallet);
    }
}