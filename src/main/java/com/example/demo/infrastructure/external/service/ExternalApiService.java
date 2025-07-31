package com.example.demo.infrastructure.external.service;

import com.example.demo.domain.port.ExternalApiPort;
import com.example.demo.infrastructure.config.ExternalApiConfig;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.InstrumentDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;
import com.example.demo.infrastructure.external.dto.AlphaVantageInfoDTO;
import com.example.demo.infrastructure.external.dto.GlobalQuoteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Implementación del puerto para consumir Alpha Vantage API
 * Adapter que conecta con servicios financieros externos
 */
@Service
public class ExternalApiService implements ExternalApiPort {

    private final RestTemplate restTemplate;
    private final ExternalApiConfig apiConfig;
    private final ObjectMapper objectMapper;

    public ExternalApiService(RestTemplate restTemplate, ExternalApiConfig apiConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public InstrumentListDTO getAllInstruments() {
        try {
            // Lista de símbolos predefinidos para buscar
            String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "NFLX", "JPM", "V", "PG", "JNJ", "WMT", "BAC", "KO", "DIS", "INTC", "Z", "T", "PFE", "MRK"};

            List<InstrumentDTO> allInstruments = new ArrayList<>();
            
            // Buscar cada símbolo y combinar los resultados
            for (String symbol : symbols) {
                try {
                    InstrumentListDTO result = searchSymbol(symbol);
                    if (result != null && result.bestMatches() != null) {
                        allInstruments.addAll(result.bestMatches());
                        // Limitar a máximo 20 instrumentos
                        if (allInstruments.size() >= 20) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Error buscando símbolo " + symbol + ": " + e.getMessage());
                    // Continuar con el siguiente símbolo si hay error
                }
            }
            
            // Limitar a exactamente 20 instrumentos
            List<InstrumentDTO> limitedInstruments = allInstruments.stream()
                .limit(20)
                .collect(Collectors.toList());
            
            return new InstrumentListDTO(limitedInstruments);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener lista de instrumentos: " + e.getMessage());
        }
    }
    
    @Override
    public InstrumentListDTO searchSymbol(String symbol) {
        try {
            String url = String.format("%s/query?function=SYMBOL_SEARCH&keywords=%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                symbol,
                apiConfig.getAlphavantage().getApiKey());
            
            String response = restTemplate.getForObject(url, String.class);
            
            // Verificar si es un mensaje de información
            if (response.contains("Information") || response.contains("Note") || response.contains("Error Message")) {
                AlphaVantageInfoDTO info = objectMapper.readValue(response, AlphaVantageInfoDTO.class);
                String message = info.information() != null ? info.information() : 
                               info.note() != null ? info.note() : info.errorMessage();
                throw new RuntimeException("Alpha Vantage: " + message);
            }
            
            return objectMapper.readValue(response, InstrumentListDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando símbolo " + symbol + ": " + e.getMessage());
        }
    }

    @Override
    public SymbolDataDTO getSymbolData(String symbol) {
        try {
            // Usar GLOBAL_QUOTE en lugar de TIME_SERIES_DAILY para compatibilidad con API key demo
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                symbol,
                apiConfig.getAlphavantage().getApiKey());
            
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("🔍 RESPUESTA SYMBOL DATA: " + response);
            
            // Verificar si es un mensaje de error
            if (response.contains("Information") || response.contains("Note") || response.contains("Error Message")) {
                AlphaVantageInfoDTO info = objectMapper.readValue(response, AlphaVantageInfoDTO.class);
                String message = info.information() != null ? info.information() : 
                               info.note() != null ? info.note() : info.errorMessage();
                throw new RuntimeException("Alpha Vantage: " + message);
            }
            
            // Parsear la respuesta de GLOBAL_QUOTE
            if (response.contains("Global Quote")) {
                GlobalQuoteDTO globalQuoteResponse = objectMapper.readValue(response, GlobalQuoteDTO.class);
                
                if (globalQuoteResponse.globalQuote() != null) {
                    // Convertir GlobalQuote a SymbolDataDTO para mantener compatibilidad
                    return convertGlobalQuoteToSymbolData(globalQuoteResponse, symbol);
                } else {
                    throw new RuntimeException("Respuesta de Global Quote vacía para símbolo: " + symbol);
                }
            }
            
            // Si no contiene Global Quote, verificar si hay datos válidos
            if (response.contains("{}") || response.trim().equals("{}")) {
                throw new RuntimeException("No se encontraron datos para el símbolo: " + symbol);
            }
            
            // Fallback: intentar parsear como SymbolDataDTO original
            return objectMapper.readValue(response, SymbolDataDTO.class);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al consumir API para símbolo " + symbol + ": " + e.getMessage());
        }
    }
    
    /**
     * Convierte la respuesta de GLOBAL_QUOTE a SymbolDataDTO para mantener compatibilidad
     */
    private SymbolDataDTO convertGlobalQuoteToSymbolData(GlobalQuoteDTO globalQuoteResponse, String symbol) {
        var quote = globalQuoteResponse.globalQuote();
        
        // Crear metadata con información del Global Quote
        var metaData = new SymbolDataDTO.MetaData(
            "Global Quote - Latest Price",
            quote.symbol() != null ? quote.symbol() : symbol,
            quote.latestTradingDay() != null ? quote.latestTradingDay() : java.time.LocalDate.now().toString(),
            "Compact", 
            "US/Eastern"
        );
        
        // Crear entrada de serie temporal con los datos del último día
        java.util.Map<String, SymbolDataDTO.DailyData> timeSeries = new java.util.HashMap<>();
        
        if (quote.latestTradingDay() != null) {
            var dailyData = new SymbolDataDTO.DailyData(
                quote.open() != null ? quote.open() : "0.00",
                quote.high() != null ? quote.high() : "0.00", 
                quote.low() != null ? quote.low() : "0.00",
                quote.price() != null ? quote.price() : "0.00",
                quote.volume() != null ? quote.volume() : "0"
            );
            
            timeSeries.put(quote.latestTradingDay(), dailyData);
        }
        
        return new SymbolDataDTO(metaData, timeSeries);
    }

    @Override
    public String getDataFromApi(String endpoint) {
        try {
            // Para endpoints personalizados
            String url = String.format("%s/query?%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                endpoint,
                apiConfig.getAlphavantage().getApiKey());
            
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consumir API: " + e.getMessage());
        }
    }

    // Métodos adicionales para Alpha Vantage
    
    public String getStockQuote(String symbol) {
        try {
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                symbol,
                apiConfig.getAlphavantage().getApiKey());
            
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al obtener cotización para " + symbol + ": " + e.getMessage());
        }
    }
    
    public String getExchangeRates(String fromCurrency, String toCurrency) {
        try {
            String url = String.format("%s/query?function=CURRENCY_EXCHANGE_RATE&from_currency=%s&to_currency=%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                fromCurrency,
                toCurrency,
                apiConfig.getAlphavantage().getApiKey());
            
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al obtener tipo de cambio: " + e.getMessage());
        }
    }
}
