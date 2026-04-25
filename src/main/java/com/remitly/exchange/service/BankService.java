package com.remitly.exchange.service;

import com.remitly.exchange.domain.BankStock;
import com.remitly.exchange.repository.BankStockRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankService {

    private final BankStockRepository bankStockRepository;

    public BankService(BankStockRepository bankStockRepository) {
        this.bankStockRepository = bankStockRepository;
    }

    @Transactional(readOnly = true)
    public List<BankStock> listAll() {
        return bankStockRepository.findAll();
    }
}