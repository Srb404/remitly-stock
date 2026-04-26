package com.remitly.exchange;

import static org.assertj.core.api.Assertions.assertThat;

import com.remitly.exchange.domain.BankStock;
import com.remitly.exchange.domain.OperationType;
import com.remitly.exchange.dto.AuditLogEntryDto;
import com.remitly.exchange.dto.ErrorResponse;
import com.remitly.exchange.dto.TradeRequest;
import com.remitly.exchange.dto.WalletStockDto;
import com.remitly.exchange.repository.AuditLogRepository;
import com.remitly.exchange.repository.BankStockRepository;
import com.remitly.exchange.repository.WalletStockRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ExchangeE2EIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
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
    void happyPath_seedBankBuyTwiceSellOnceAndInspectEverything() {
        seed(new BankStock("AAPL", 10), new BankStock("MSFT", 5));

        ResponseEntity<WalletStockDto> buy1 = trade("w1", "AAPL", OperationType.BUY);
        assertThat(buy1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buy1.getBody().quantity()).isEqualTo(1);

        ResponseEntity<WalletStockDto> buy2 = trade("w1", "AAPL", OperationType.BUY);
        assertThat(buy2.getBody().quantity()).isEqualTo(2);

        ResponseEntity<WalletStockDto> sell = trade("w1", "AAPL", OperationType.SELL);
        assertThat(sell.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sell.getBody().quantity()).isEqualTo(1);

        ResponseEntity<List<AuditLogEntryDto>> log = restTemplate.exchange(
                "/log", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertThat(log.getBody())
                .extracting(AuditLogEntryDto::type)
                .containsExactly(OperationType.BUY, OperationType.BUY, OperationType.SELL);
    }

    @Test
    void buy_unknownStock_returns404() {
        seed(new BankStock("AAPL", 1));
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/wallets/w1/stocks/GOOG", HttpMethod.POST,
                new HttpEntity<>(new TradeRequest(OperationType.BUY)), ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isEqualTo("stock_not_found");
    }

    private void seed(BankStock... stocks) {
        bankStockRepository.saveAllAndFlush(List.of(stocks));
    }

    private ResponseEntity<WalletStockDto> trade(String walletId, String stockName, OperationType type) {
        return restTemplate.exchange(
                "/wallets/" + walletId + "/stocks/" + stockName,
                HttpMethod.POST, new HttpEntity<>(new TradeRequest(type)), WalletStockDto.class);
    }
}
