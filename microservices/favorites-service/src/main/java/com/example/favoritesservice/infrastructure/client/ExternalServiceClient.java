package com.example.favoritesservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "external-service", url = "http://external-service:8083")
public interface ExternalServiceClient {
    
    @GetMapping("/api/external/validate/{symbol}")
    boolean validateSymbol(@PathVariable("symbol") String symbol);
}
