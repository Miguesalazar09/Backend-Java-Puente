package com.example.demo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO para datos específicos de un símbolo financiero de Alpha Vantage
 */
public record SymbolDataDTO(
    @JsonProperty("Meta Data") MetaData metaData,
    @JsonProperty("Time Series (Daily)") Map<String, DailyData> timeSeries
) {
    
    public record MetaData(
        @JsonProperty("1. Information") String information,
        @JsonProperty("2. Symbol") String symbol,
        @JsonProperty("3. Last Refreshed") String lastRefreshed,
        @JsonProperty("4. Output Size") String outputSize,
        @JsonProperty("5. Time Zone") String timeZone
    ) {}
    
    public record DailyData(
        @JsonProperty("1. open") String open,
        @JsonProperty("2. high") String high,
        @JsonProperty("3. low") String low,
        @JsonProperty("4. close") String close,
        @JsonProperty("5. volume") String volume
    ) {}
}
