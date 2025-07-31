package com.example.demo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para la respuesta de búsqueda de símbolos de Alpha Vantage
 * Estructura real: {"bestMatches": [...]}
 */
public record InstrumentListDTO(
    @JsonProperty("bestMatches") List<InstrumentDTO> bestMatches
) {}
