package com.demo.reactive.model;

import java.math.BigDecimal;
import java.time.Instant;

public class StockPrice {
    
    private String symbol;
    
    private BigDecimal price;
    
    private Double changePercent;
    
    private Long timestamp;

    public StockPrice() {
    }

    public StockPrice(String symbol, BigDecimal price, Double changePercent, Long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
        this.timestamp = timestamp;
    }
    
    public StockPrice(String symbol, BigDecimal price, Double changePercent) {
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StockPrice{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", changePercent=" + changePercent +
                ", timestamp=" + timestamp +
                '}';
    }
}
