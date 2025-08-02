package com.example.externalservice.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;

@Service
@Profile("!mock")
public class ExternalDataService implements IStockService {
    
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9]{1,10}$");
    
    @Value("${external.api.alphavantage.base-url}")
    private String baseUrl;
    
    @Value("${external.api.alphavantage.api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ExternalDataService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Cacheable(value = "symbolValidation", key = "#symbol.toUpperCase()")
    @Override
    public ValidationResult validateSymbol(String symbol) {
        if (!isValidSymbolFormat(symbol)) {
            return new ValidationResult(false, "Formato de símbolo inválido");
        }
        
        try {
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                baseUrl, symbol.toUpperCase().trim(), apiKey);
            
            String response = restTemplate.getForObject(url, String.class);
            
            // Verificar si hay errores o respuesta vacía
            if (response == null || response.contains("Error Message")) {
                return new ValidationResult(false, "Símbolo no encontrado en Alpha Vantage");
            }
            
            // Verificar si contiene datos válidos del quote global
            if (response.contains("Global Quote") && !response.contains("{}")) {
                return new ValidationResult(true, "Símbolo válido");
            } else {
                return new ValidationResult(false, "Símbolo no encontrado en Alpha Vantage");
            }
        } catch (Exception e) {
            return new ValidationResult(false, "Error validando símbolo: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "symbolData", key = "#symbol.toUpperCase()")
    @Override
    public String getSymbolData(String symbol) {
        if (!isValidSymbolFormat(symbol)) {
            throw new IllegalArgumentException("Formato de símbolo inválido");
        }
        
        try {
            // Cambiar a TIME_SERIES_DAILY para obtener series históricas diarias
            String url = String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
                baseUrl, symbol.toUpperCase().trim(), apiKey);
            
            String response = restTemplate.getForObject(url, String.class);
            
            // Verificar si hay errores en la respuesta
            if (response != null && (response.contains("Error Message") || 
                                   response.contains("Note: ") ||
                                   response.contains("Thank you for using Alpha Vantage"))) {
                throw new RuntimeException("API Error: " + response);
            }
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo datos de series temporales para " + symbol + ": " + e.getMessage());
        }
    }
    
    private boolean isValidSymbolFormat(String symbol) {
        return symbol != null && 
               !symbol.trim().isEmpty() && 
               SYMBOL_PATTERN.matcher(symbol.toUpperCase().trim()).matches();
    }
    
    @CacheEvict(value = {"symbolValidation", "symbolData"}, key = "#symbol.toUpperCase()")
    public void evictCache(String symbol) {
        // Método para limpiar caché de un símbolo específico
    }
    
    @CacheEvict(value = {"symbolValidation", "symbolData"}, allEntries = true)
    public void evictAllCache() {
        // Método para limpiar toda la caché
    }
}
