package com.example.demo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la respuesta de GLOBAL_QUOTE de Alpha Vantage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GlobalQuoteDTO(
    @JsonProperty("Global Quote") GlobalQuoteData globalQuote
) {
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GlobalQuoteData(
        @JsonProperty("01. symbol") String symbol,
        @JsonProperty("02. open") String open,
        @JsonProperty("03. high") String high,
        @JsonProperty("04. low") String low,
        @JsonProperty("05. price") String price,
        @JsonProperty("06. volume") String volume,
        @JsonProperty("07. latest trading day") String latestTradingDay,
        @JsonProperty("08. previous close") String previousClose,
        @JsonProperty("09. change") String change,
        @JsonProperty("10. change percent") String changePercent
    ) {}
}
