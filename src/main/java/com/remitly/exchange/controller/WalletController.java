package com.remitly.exchange.controller;

import com.remitly.exchange.domain.OperationType;
import com.remitly.exchange.dto.TradeRequest;
import com.remitly.exchange.dto.WalletResponse;
import com.remitly.exchange.dto.WalletStockDto;
import com.remitly.exchange.dto.WalletStockEntryDto;
import com.remitly.exchange.exception.StockNotFoundException;
import com.remitly.exchange.service.TradingService;
import com.remitly.exchange.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;
    private final TradingService tradingService;

    public WalletController(WalletService walletService, TradingService tradingService) {
        this.walletService = walletService;
        this.tradingService = tradingService;
    }

    @GetMapping("/{walletId}")
    public WalletResponse get(@PathVariable String walletId) {
        List<WalletStockEntryDto> stocks = walletService.listByWallet(walletId).stream()
                .map(WalletStockEntryDto::from)
                .toList();
        return new WalletResponse(walletId, stocks);
    }

    @GetMapping("/{walletId}/stocks/{stockName}")
    public WalletStockDto getStock(@PathVariable String walletId, @PathVariable String stockName) {
        return walletService.findOne(walletId, stockName)
                .map(WalletStockDto::from)
                .orElseThrow(() -> new StockNotFoundException(stockName));
    }

    @PostMapping("/{walletId}/stocks/{stockName}")
    @ResponseStatus(HttpStatus.OK)
    public WalletStockDto trade(
            @PathVariable String walletId,
            @PathVariable String stockName,
            @RequestBody @Valid TradeRequest request) {
        return request.type() == OperationType.BUY
                ? tradingService.buy(walletId, stockName)
                : tradingService.sell(walletId, stockName);
    }
}