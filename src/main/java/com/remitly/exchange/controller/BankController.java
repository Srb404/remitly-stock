package com.remitly.exchange.controller;

import com.remitly.exchange.dto.BankStockDto;
import com.remitly.exchange.dto.SetBankRequest;
import com.remitly.exchange.service.BankService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BankStockDto> set(@Valid @RequestBody SetBankRequest request) {
        return bankService.replaceAll(request.stocks()).stream()
                .map(BankStockDto::from)
                .toList();
    }
}