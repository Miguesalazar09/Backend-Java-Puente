package com.example.demo.infrastructure.external.service;

import com.example.demo.domain.port.ExternalApiPort;
import com.example.demo.infrastructure.config.ExternalApiConfig;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.InstrumentDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;
import com.example.demo.infrastructure.external.dto.AlphaVantageInfoDTO;
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
            //String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "NFLX"};
            String[] symbols = {"AAPL", "MSFT", "GOOGL"};

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
    
    private InstrumentListDTO searchSymbol(String symbol) {
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
            // Endpoint para obtener datos diarios de un símbolo
            String url = String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
                apiConfig.getAlphavantage().getBaseUrl(),
                symbol,
                apiConfig.getAlphavantage().getApiKey());
            
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("🔍 RESPUESTA SYMBOL DATA: " + response);
            
            // Verificar si es un mensaje de información
            if (response.contains("Information") || response.contains("Note") || response.contains("Error Message")) {
                AlphaVantageInfoDTO info = objectMapper.readValue(response, AlphaVantageInfoDTO.class);
                String message = info.information() != null ? info.information() : 
                               info.note() != null ? info.note() : info.errorMessage();
                throw new RuntimeException("Alpha Vantage: " + message);
            }
            
            return objectMapper.readValue(response, SymbolDataDTO.class);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al consumir API para símbolo " + symbol + ": " + e.getMessage());
        }
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
