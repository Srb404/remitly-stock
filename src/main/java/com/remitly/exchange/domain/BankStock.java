package com.remitly.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_stocks")
public class BankStock {

    @Id
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    protected BankStock() {
    }

    public BankStock(String name, long quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}