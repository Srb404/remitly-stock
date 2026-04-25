package com.remitly.exchange.repository;

import com.remitly.exchange.domain.BankStock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankStockRepository extends JpaRepository<BankStock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankStock b where b.name = :name")
    Optional<BankStock> findForUpdate(@Param("name") String name);
}