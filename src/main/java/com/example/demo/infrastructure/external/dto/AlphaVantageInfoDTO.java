package com.example.demo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para respuestas de información/error de Alpha Vantage
 */
public record AlphaVantageInfoDTO(
    @JsonProperty("Information") String information,
    @JsonProperty("Note") String note,
    @JsonProperty("Error Message") String errorMessage
) {}
