package com.example.demo.application.usecase;

import com.example.demo.domain.port.ExternalApiPort;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;
import org.springframework.stereotype.Service;

/**
 * Use Case para gestionar datos de Alpha Vantage API
 * Contiene la lógica de negocio para consumir servicios financieros externos
 */
@Service
public class ExternalDataUseCase {
    
    private final ExternalApiPort externalApiPort;
    
    public ExternalDataUseCase(ExternalApiPort externalApiPort) {
        this.externalApiPort = externalApiPort;
    }
    
    public InstrumentListDTO getAllInstruments() {
        try {
            return externalApiPort.getAllInstruments();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener instrumentos financieros: " + e.getMessage());
        }
    }
    
    public SymbolDataDTO getSymbolData(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Símbolo no puede estar vacío");
        }
        
        try {
            SymbolDataDTO data = externalApiPort.getSymbolData(symbol.toUpperCase());
            if (data == null) {
                throw new RuntimeException("Datos no encontrados para el símbolo: " + symbol);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener datos para símbolo " + symbol + ": " + e.getMessage());
        }
    }
    
    public String getGenericData(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint no puede estar vacío");
        }
        
        try {
            return externalApiPort.getDataFromApi(endpoint);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener datos de " + endpoint + ": " + e.getMessage());
        }
    }
}
