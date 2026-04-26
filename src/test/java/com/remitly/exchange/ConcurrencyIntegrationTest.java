package com.remitly.exchange;

import static org.assertj.core.api.Assertions.assertThat;

import com.remitly.exchange.domain.BankStock;
import com.remitly.exchange.domain.WalletStock;
import com.remitly.exchange.repository.AuditLogRepository;
import com.remitly.exchange.repository.BankStockRepository;
import com.remitly.exchange.repository.WalletStockRepository;
import com.remitly.exchange.service.TradingService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ConcurrencyIntegrationTest {

    private static final String STOCK = "AAPL";

    @Autowired private TradingService tradingService;
    @Autowired private BankStockRepository bankStockRepository;
    @Autowired private WalletStockRepository walletStockRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    @BeforeEach
    void resetState() {
        auditLogRepository.deleteAllInBatch();
        walletStockRepository.deleteAllInBatch();
        bankStockRepository.deleteAllInBatch();
    }

    @Test
    void parallelBuysAcrossManyWallets_preserveBankInvariantAndLogEveryTrade() throws Exception {
        int wallets = 100;
        bankStockRepository.saveAndFlush(new BankStock(STOCK, wallets));

        runInParallel(wallets, i -> tradingService.buy("w-" + i, STOCK));

        assertThat(bankStockRepository.findById(STOCK).orElseThrow().getQuantity()).isZero();
        long walletTotal = walletStockRepository.findAll().stream()
                .mapToLong(WalletStock::getQuantity).sum();
        assertThat(walletTotal).isEqualTo(wallets);
        assertThat(auditLogRepository.count()).isEqualTo(wallets);
    }

    @Test
    void interleavedBuysAndSells_onSharedWallet_preserveTotalInvariant() throws Exception {
        int ops = 100;
        long startingBank = 100, startingWallet = 100;
        bankStockRepository.saveAndFlush(new BankStock(STOCK, startingBank));
        walletStockRepository.saveAndFlush(new WalletStock("shared", STOCK, startingWallet));

        runInParallel(ops, i -> {
            if (i % 2 == 0) tradingService.buy("shared", STOCK);
            else tradingService.sell("shared", STOCK);
        });

        long bank = bankStockRepository.findById(STOCK).orElseThrow().getQuantity();
        long wallet = walletStockRepository.findAll().stream()
                .mapToLong(WalletStock::getQuantity).sum();
        assertThat(bank + wallet).isEqualTo(startingBank + startingWallet);
        assertThat(auditLogRepository.count()).isEqualTo(ops);
    }

    private void runInParallel(int n, java.util.function.IntConsumer task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(n, 32));
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < n; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> { start.await(); task.accept(idx); return null; }));
            }
            start.countDown();
            for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
