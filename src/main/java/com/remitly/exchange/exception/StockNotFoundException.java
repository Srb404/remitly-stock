package com.remitly.exchange.exception;

public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(String stockName) {
        super("Stock not found: " + stockName);
    }
}