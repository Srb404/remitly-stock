package com.remitly.exchange.controller;

import com.remitly.exchange.dto.BankStockDto;
import com.remitly.exchange.service.BankService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping
    public List<BankStockDto> list() {
        return bankService.listAll().stream().map(BankStockDto::from).toList();
    }
}