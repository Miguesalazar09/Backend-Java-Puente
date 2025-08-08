package com.example.externalservice.application.usecase;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"mock", "mock-redis"})
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
            "ORCL", "CRM", "ADBE", "NOW", "ZM", "SHOP", "ROKU", "PINS",
            "IBM", "JPM", "JNJ", "WMT", "PG", "V", "MA", "HD", "DIS", "KO",
            "PFE", "XOM", "VZ", "CSCO", "PEP", "TMO", "ABT", "COST", "AVGO", "TXN"
        };
        
        for (String validSymbol : validSymbols) {
            if (validSymbol.equals(upperSymbol)) {
                return new ValidationResult(true, "Símbolo válido (Mock)");
            }
        }
        
        return new ValidationResult(false, "Símbolo no encontrado en datos mock");
    }
    
    @Override
    @Cacheable(value = "symbolData", key = "#symbol.toUpperCase()")
    public String getSymbolData(String symbol) {
        ValidationResult validation = validateSymbol(symbol);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // Log para verificar que se está generando (no desde cache)
        System.out.println("Generando datos mock para símbolo: " + upperSymbol + " (NO desde cache)");
        
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
            case "IBM" -> generateMockData("International Business Machines Corporation", "185.75", "188.20", "184.30", "186.90", "8765400");
            case "JPM" -> generateMockData("JPMorgan Chase & Co.", "178.40", "180.15", "177.20", "179.30", "12543600");
            case "JNJ" -> generateMockData("Johnson & Johnson", "162.80", "164.50", "161.90", "163.70", "9876500");
            case "WMT" -> generateMockData("Walmart Inc.", "158.90", "160.45", "157.80", "159.60", "11234700");
            case "PG" -> generateMockData("Procter & Gamble Company", "142.30", "143.80", "141.50", "142.95", "7654300");
            case "V" -> generateMockData("Visa Inc.", "278.50", "281.20", "276.80", "279.40", "15432100");
            case "MA" -> generateMockData("Mastercard Incorporated", "425.60", "428.90", "423.40", "426.80", "8765400");
            case "HD" -> generateMockData("Home Depot Inc.", "365.20", "368.75", "363.40", "366.50", "12109800");
            case "DIS" -> generateMockData("Walt Disney Company", "95.40", "97.20", "94.60", "96.30", "18765400");
            case "KO" -> generateMockData("Coca-Cola Company", "59.80", "60.45", "59.20", "60.10", "14567800");
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
