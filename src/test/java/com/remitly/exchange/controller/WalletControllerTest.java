package com.remitly.exchange.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.remitly.exchange.domain.WalletStock;
import com.remitly.exchange.dto.WalletStockDto;
import com.remitly.exchange.exception.GlobalExceptionHandler;
import com.remitly.exchange.exception.InsufficientStockException;
import com.remitly.exchange.service.BankService;
import com.remitly.exchange.service.TradingService;
import com.remitly.exchange.service.WalletService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private TradingService tradingService;

    @MockitoBean
    private BankService bankService;

    @Test
    void getWallet_returnsStocks() throws Exception {
        when(walletService.listByWallet("w1")).thenReturn(List.of(
                new WalletStock("w1", "AAPL", 3)));

        mockMvc.perform(get("/wallets/w1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("w1"))
                .andExpect(jsonPath("$.stocks", hasSize(1)))
                .andExpect(jsonPath("$.stocks[0].name").value("AAPL"))
                .andExpect(jsonPath("$.stocks[0].quantity").value(3));
    }

    @Test
    void postTradeBuy_invokesBuyAndReturnsWalletState() throws Exception {
        when(tradingService.buy("w1", "AAPL"))
                .thenReturn(new WalletStockDto("w1", "AAPL", 1));

        mockMvc.perform(post("/wallets/w1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"buy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1));

        verify(tradingService).buy("w1", "AAPL");
    }

    @Test
    void postTradeSell_insufficientStock_returns400() throws Exception {
        doThrow(new InsufficientStockException("wallet empty"))
                .when(tradingService).sell("w1", "AAPL");

        mockMvc.perform(post("/wallets/w1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"sell\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("insufficient_stock"));
    }

    @Test
    void getWalletStock_returnsPlainNumber() throws Exception {
        when(bankService.exists("AAPL")).thenReturn(true);
        when(walletService.findOne("w1", "AAPL"))
                .thenReturn(java.util.Optional.of(new WalletStock("w1", "AAPL", 99)));

        mockMvc.perform(get("/wallets/w1/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("99"));
    }

    @Test
    void getWalletStock_walletEmpty_returnsZero() throws Exception {
        when(bankService.exists("AAPL")).thenReturn(true);
        when(walletService.findOne("w1", "AAPL")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/wallets/w1/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getWalletStock_unknownStock_returns404() throws Exception {
        when(bankService.exists("ZZZ")).thenReturn(false);

        mockMvc.perform(get("/wallets/w1/stocks/ZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("stock_not_found"));
    }

    @Test
    void postTrade_invalidType_returns400() throws Exception {
        mockMvc.perform(post("/wallets/w1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"gift\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("malformed_request"));
    }
}
