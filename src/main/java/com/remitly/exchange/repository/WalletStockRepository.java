package com.remitly.exchange.repository;

import com.remitly.exchange.domain.WalletStock;
import com.remitly.exchange.domain.WalletStockId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletStockRepository extends JpaRepository<WalletStock, WalletStockId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletStock w where w.walletId = :walletId and w.stockName = :stockName")
    Optional<WalletStock> findForUpdate(@Param("walletId") String walletId, @Param("stockName") String stockName);

    List<WalletStock> findByWalletId(String walletId);
}