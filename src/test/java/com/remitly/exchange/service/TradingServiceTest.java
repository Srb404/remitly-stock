package com.remitly.exchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    private static final String WALLET = "wallet-1";
    private static final String STOCK = "AAPL";
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-21T10:15:30Z");

    @Mock
    private BankStockRepository bankStockRepository;

    @Mock
    private WalletStockRepository walletStockRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private final Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private TradingService tradingService;
    private BankStock bankStock;
    private WalletStock walletStock;

    @BeforeEach
    void setUp() {
        tradingService = new TradingService(
                bankStockRepository, walletStockRepository, auditLogRepository, clock);
        bankStock = new BankStock(STOCK, 10);
        walletStock = new WalletStock(WALLET, STOCK, 5);
    }

    @Test
    void buy_happyPath_decrementsBankIncrementsWalletAndLogs() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.of(bankStock));
        when(walletStockRepository.findForUpdate(WALLET, STOCK)).thenReturn(Optional.of(walletStock));

        WalletStockDto result = tradingService.buy(WALLET, STOCK);

        assertThat(bankStock.getQuantity()).isEqualTo(9);
        assertThat(walletStock.getQuantity()).isEqualTo(6);
        assertThat(result).isEqualTo(new WalletStockDto(WALLET, STOCK, 6));

        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLogEntry saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(OperationType.BUY);
        assertThat(saved.getCreatedAt().toInstant()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void buy_firstTimeForWallet_createsWalletStockRow() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.of(bankStock));
        when(walletStockRepository.findForUpdate(WALLET, STOCK)).thenReturn(Optional.empty());
        ArgumentCaptor<WalletStock> newWallet = ArgumentCaptor.forClass(WalletStock.class);
        when(walletStockRepository.save(newWallet.capture())).thenAnswer(inv -> inv.getArgument(0));

        WalletStockDto result = tradingService.buy(WALLET, STOCK);

        assertThat(newWallet.getValue().getQuantity()).isEqualTo(1);
        assertThat(result).isEqualTo(new WalletStockDto(WALLET, STOCK, 1));
    }

    @Test
    void buy_bankEmpty_throwsInsufficientStock() {
        bankStock.setQuantity(0);
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.of(bankStock));

        assertThatThrownBy(() -> tradingService.buy(WALLET, STOCK))
                .isInstanceOf(InsufficientStockException.class);

        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void buy_stockUnknown_throwsStockNotFound() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradingService.buy(WALLET, STOCK))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    void sell_happyPath_decrementsWalletIncrementsBankAndLogs() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.of(bankStock));
        when(walletStockRepository.findForUpdate(WALLET, STOCK)).thenReturn(Optional.of(walletStock));

        WalletStockDto result = tradingService.sell(WALLET, STOCK);

        assertThat(walletStock.getQuantity()).isEqualTo(4);
        assertThat(bankStock.getQuantity()).isEqualTo(11);
        assertThat(result).isEqualTo(new WalletStockDto(WALLET, STOCK, 4));
    }

    @Test
    void sell_walletMissingStock_throwsInsufficientStock() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.of(bankStock));
        when(walletStockRepository.findForUpdate(WALLET, STOCK)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradingService.sell(WALLET, STOCK))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void sell_stockUnknown_throwsStockNotFound() {
        when(bankStockRepository.findForUpdate(STOCK)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradingService.sell(WALLET, STOCK))
                .isInstanceOf(StockNotFoundException.class);
    }
}
