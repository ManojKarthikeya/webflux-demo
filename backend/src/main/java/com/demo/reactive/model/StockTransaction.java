package com.demo.reactive.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("stock_transactions")
public class StockTransaction {
    
    @Id
    private Long id;
    
    private String userId;
    
    private String symbol;
    
    private Integer quantity;
    
    private BigDecimal pricePerShare;
    
    private String transactionType;
    
    private LocalDateTime createdAt;

    public StockTransaction() {
    }

    public StockTransaction(Long id, String userId, String symbol, Integer quantity, BigDecimal pricePerShare, String transactionType, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
    }
    
    public StockTransaction(String userId, String symbol, Integer quantity, 
                           BigDecimal pricePerShare, String transactionType) {
        this.userId = userId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.transactionType = transactionType;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StockTransaction{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", pricePerShare=" + pricePerShare +
                ", transactionType='" + transactionType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
