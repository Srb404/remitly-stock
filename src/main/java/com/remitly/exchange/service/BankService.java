package com.remitly.exchange.service;

import com.remitly.exchange.domain.BankStock;
import com.remitly.exchange.dto.BankStockDto;
import com.remitly.exchange.repository.BankStockRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    @Transactional(readOnly = true)
    public boolean exists(String stockName) {
        return bankStockRepository.existsById(stockName);
    }

    @Transactional
    public List<BankStock> replaceAll(List<BankStockDto> desired) {
        bankStockRepository.lockTableExclusive();

        Set<String> desiredNames = desired.stream()
                .map(BankStockDto::name)
                .collect(Collectors.toCollection(HashSet::new));

        List<String> toDelete = bankStockRepository.findAll().stream()
                .map(BankStock::getName)
                .filter(name -> !desiredNames.contains(name))
                .toList();
        if (!toDelete.isEmpty()) {
            bankStockRepository.deleteAllByIdInBatch(toDelete);
        }

        List<BankStock> upserted = desired.stream()
                .map(dto -> new BankStock(dto.name(), dto.quantity()))
                .toList();
        bankStockRepository.saveAll(upserted);
        bankStockRepository.flush();

        return bankStockRepository.findAll();
    }
}