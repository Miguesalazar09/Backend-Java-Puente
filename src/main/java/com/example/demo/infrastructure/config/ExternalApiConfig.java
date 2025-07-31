package com.example.demo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada para Alpha Vantage API
 * Permite cambiar URLs y keys sin recompilar el código
 */
@Configuration
@ConfigurationProperties(prefix = "external.api")
public class ExternalApiConfig {

    private Alphavantage alphavantage = new Alphavantage();

    // Getters y Setters
    public Alphavantage getAlphavantage() { return alphavantage; }
    public void setAlphavantage(Alphavantage alphavantage) { this.alphavantage = alphavantage; }

    // Clase para Alpha Vantage API
    public static class Alphavantage {
        private String baseUrl;
        private String apiKey;
        private int timeout;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
}
