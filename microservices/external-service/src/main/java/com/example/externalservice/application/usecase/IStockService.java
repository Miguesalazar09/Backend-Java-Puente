package com.example.externalservice.application.usecase;

public interface IStockService {
    ValidationResult validateSymbol(String symbol);
    String getSymbolData(String symbol);
    
    record ValidationResult(boolean valid, String message) {}
}
