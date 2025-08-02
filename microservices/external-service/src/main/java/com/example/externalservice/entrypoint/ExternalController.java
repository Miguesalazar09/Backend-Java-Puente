package com.example.externalservice.entrypoint;

import com.example.externalservice.application.usecase.IStockService;
import com.example.externalservice.application.usecase.IStockService.ValidationResult;
import com.example.externalservice.application.usecase.ExternalDataService;
import com.example.externalservice.application.service.InstrumentCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/external")
public class ExternalController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalController.class);
    
    private final IStockService stockService;
    private final ExternalDataService externalDataService; // Opcional para métodos de caché
    private final InstrumentCacheService instrumentCacheService;

    public ExternalController(IStockService stockService, 
                            @org.springframework.beans.factory.annotation.Autowired(required = false) 
                            ExternalDataService externalDataService,
                            InstrumentCacheService instrumentCacheService) {
        this.stockService = stockService;
        this.externalDataService = externalDataService;
        this.instrumentCacheService = instrumentCacheService;
    }

    @GetMapping("/validate/{symbol}")
    public ResponseEntity<Boolean> validateSymbol(@PathVariable String symbol) {
        try {
            ValidationResult result = stockService.validateSymbol(symbol);
            return ResponseEntity.ok(result.valid());
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/instruments/{symbol}")
    public ResponseEntity<String> getSymbolData(@PathVariable String symbol) {
        try {
            String data = stockService.getSymbolData(symbol);
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"error\": \"Error interno del servidor\"}");
        }
    }

    @GetMapping("/instruments/list")
    public ResponseEntity<List<String>> getAvailableInstruments() {
        try {
            List<String> instruments = instrumentCacheService.getAvailableInstruments();
            logger.info("📊 Retornando {} instrumentos disponibles", instruments.size());
            return ResponseEntity.ok(instruments);
        } catch (Exception e) {
            logger.error("❌ Error getting available instruments: {}", e.getMessage());
            // Fallback básico
            return ResponseEntity.ok(Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"));
        }
    }

    @DeleteMapping("/cache/{symbol}")
    public ResponseEntity<String> evictCacheForSymbol(@PathVariable String symbol) {
        try {
            if (externalDataService != null) {
                externalDataService.evictCache(symbol);
                return ResponseEntity.ok("Caché eliminada para el símbolo: " + symbol.toUpperCase());
            } else {
                return ResponseEntity.ok("Mock service - Sin caché para eliminar: " + symbol.toUpperCase());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error eliminando caché: " + e.getMessage());
        }
    }

    @DeleteMapping("/cache")
    public ResponseEntity<String> evictAllCache() {
        try {
            if (externalDataService != null) {
                externalDataService.evictAllCache();
                return ResponseEntity.ok("Toda la caché ha sido eliminada");
            } else {
                return ResponseEntity.ok("Mock service - Sin caché para eliminar");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error eliminando caché: " + e.getMessage());
        }
    }

    @DeleteMapping("/cache/instruments")
    public ResponseEntity<Map<String, String>> clearInstrumentsCache() {
        try {
            instrumentCacheService.clearInstrumentsCache();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache de instrumentos limpiada exitosamente");
            response.put("timestamp", LocalDateTime.now().toString());
            logger.info("🗑️  Cache de instrumentos limpiada manualmente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error clearing instruments cache: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/cache/instruments/all")
    public ResponseEntity<Map<String, String>> clearAllInstrumentsCache() {
        try {
            instrumentCacheService.clearAllCache();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Toda la cache de instrumentos limpiada exitosamente");
            response.put("timestamp", LocalDateTime.now().toString());
            logger.info("🗑️  Toda la cache de instrumentos limpiada manualmente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error clearing all instruments cache: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint temporal para debug
    @GetMapping("/debug/{symbol}")
    public ResponseEntity<String> debugSymbol(@PathVariable String symbol) {
        try {
            String data = stockService.getSymbolData(symbol);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    public record Instrument(String symbol, String name) {}
    public record InstrumentListResponse(boolean success, String message, java.util.List<Instrument> instruments) {}
}
