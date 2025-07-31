package com.example.demo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO para respuestas de información/error de Alpha Vantage
 * Incluye campos para diferentes tipos de respuestas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlphaVantageInfoDTO(
    @JsonProperty("Information") String information,
    @JsonProperty("Note") String note,
    @JsonProperty("Error Message") String errorMessage,
    @JsonProperty("Meta Data") MetaDataDTO metaData,
    @JsonProperty("Time Series (Daily)") Map<String, DailyDataDTO> timeSeries
) {
    
    /**
     * DTO para Meta Data de respuestas de Alpha Vantage
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetaDataDTO(
        @JsonProperty("1. Information") String information,
        @JsonProperty("2. Symbol") String symbol,
        @JsonProperty("3. Last Refreshed") String lastRefreshed,
        @JsonProperty("4. Output Size") String outputSize,
        @JsonProperty("5. Time Zone") String timeZone
    ) {}
    
    /**
     * DTO para datos diarios de serie temporal
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyDataDTO(
        @JsonProperty("1. open") String open,
        @JsonProperty("2. high") String high,
        @JsonProperty("3. low") String low,
        @JsonProperty("4. close") String close,
        @JsonProperty("5. volume") String volume
    ) {}
}
