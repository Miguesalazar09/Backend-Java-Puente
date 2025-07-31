package com.example.demo.domain.port;

import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;

/**
 * Port para consumir datos de Alpha Vantage API
 * Define el contrato sin depender de implementaciones específicas
 */
public interface ExternalApiPort {
    
    // Obtener lista de instrumentos financieros
    InstrumentListDTO getAllInstruments();
    
    // Obtener datos específicos de un símbolo
    SymbolDataDTO getSymbolData(String symbol);
    
    // Método genérico para otros endpoints
    String getDataFromApi(String endpoint);
}
