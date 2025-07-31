package com.example.demo.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Favorite {
    private UUID id;
    private UUID userId;
    private String symbol;
    private LocalDateTime createdAt;

    public Favorite(UUID id, UUID userId, String symbol, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.symbol = symbol;
        this.createdAt = createdAt;
    }

    // Constructor sin ID para crear nuevos favoritos
    public Favorite(UUID userId, String symbol) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
