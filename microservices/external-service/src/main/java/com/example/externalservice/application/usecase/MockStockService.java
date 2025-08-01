package com.example.externalservice.application.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockStockService implements IStockService {
    
    @Override
    public ValidationResult validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ValidationResult(false, "Símbolo no puede estar vacío");
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // Lista de símbolos válidos para el mock
        String[] validSymbols = {
            "AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "META", "NVDA", "NFLX",
            "UBER", "SPOT", "PYPL", "SQ", "TWTR", "SNAP", "AMD", "INTC",
            "ORCL", "CRM", "ADBE", "NOW", "ZM", "SHOP", "ROKU", "PINS"
        };
        
        for (String validSymbol : validSymbols) {
            if (validSymbol.equals(upperSymbol)) {
                return new ValidationResult(true, "Símbolo válido (Mock)");
            }
        }
        
        return new ValidationResult(false, "Símbolo no encontrado en datos mock");
    }
    
    @Override
    public String getSymbolData(String symbol) {
        ValidationResult validation = validateSymbol(symbol);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // Simular datos realistas pero ficticios para diferentes símbolos
        String mockData = switch (upperSymbol) {
            case "AAPL" -> generateMockData("Apple Inc.", "175.43", "176.80", "174.20", "175.10", "52434500");
            case "GOOGL" -> generateMockData("Alphabet Inc Class A", "138.25", "139.70", "137.50", "138.80", "28542300");
            case "MSFT" -> generateMockData("Microsoft Corporation", "415.26", "417.50", "413.80", "416.05", "21567800");
            case "TSLA" -> generateMockData("Tesla Inc", "248.42", "252.75", "246.10", "249.83", "45123600");
            case "AMZN" -> generateMockData("Amazon.com Inc", "155.89", "157.20", "154.30", "156.45", "35678900");
            case "META" -> generateMockData("Meta Platforms Inc", "512.75", "516.40", "510.20", "514.30", "18943200");
            case "NVDA" -> generateMockData("NVIDIA Corporation", "875.30", "882.50", "870.40", "878.90", "42156700");
            case "NFLX" -> generateMockData("Netflix Inc", "485.60", "488.75", "482.40", "486.20", "12847500");
            default -> generateMockData(upperSymbol + " Corporation", "125.50", "127.80", "124.20", "126.35", "15234800");
        };
        
        return mockData;
    }
    
    private String generateMockData(String companyName, String open, String high, String low, String close, String volume) {
        return String.format("""
            {
                "Meta Data": {
                    "1. Information": "Daily Prices (open, high, low, close) and Volumes",
                    "2. Symbol": "%s",
                    "3. Last Refreshed": "2024-01-15",
                    "4. Output Size": "Compact",
                    "5. Time Zone": "US/Eastern"
                },
                "Time Series (Daily)": {
                    "2024-01-15": {
                        "1. open": "%s",
                        "2. high": "%s",
                        "3. low": "%s",
                        "4. close": "%s",
                        "5. volume": "%s"
                    },
                    "2024-01-12": {
                        "1. open": "%.2f",
                        "2. high": "%.2f",
                        "3. low": "%.2f",
                        "4. close": "%.2f",
                        "5. volume": "%s"
                    }
                }
            }
            """, 
            companyName, open, high, low, close, volume,
            Float.parseFloat(close) - 2.5f,
            Float.parseFloat(high) - 1.8f,
            Float.parseFloat(low) - 2.1f,
            Float.parseFloat(close) - 1.9f,
            volume
        );
    }
}
