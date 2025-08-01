package com.example.externalservice.entrypoint;

import com.example.externalservice.application.usecase.IStockService;
import com.example.externalservice.application.usecase.IStockService.ValidationResult;
import com.example.externalservice.application.usecase.ExternalDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalController {

    private final IStockService stockService;
    private final ExternalDataService externalDataService; // Opcional para métodos de caché

    public ExternalController(IStockService stockService, 
                            @org.springframework.beans.factory.annotation.Autowired(required = false) 
                            ExternalDataService externalDataService) {
        this.stockService = stockService;
        this.externalDataService = externalDataService;
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
    public ResponseEntity<InstrumentListResponse> getAllInstruments() {
        try {
            // Simulamos algunos instrumentos para prueba
            return ResponseEntity.ok(new InstrumentListResponse(true, "Instrumentos obtenidos", 
                java.util.List.of(
                    new Instrument("AAPL", "Apple Inc."),
                    new Instrument("GOOGL", "Alphabet Inc."),
                    new Instrument("MSFT", "Microsoft Corporation"),
                    new Instrument("TSLA", "Tesla Inc.")
                )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new InstrumentListResponse(false, "Error: " + e.getMessage(), null));
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
