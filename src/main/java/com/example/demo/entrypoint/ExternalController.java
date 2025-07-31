package com.example.demo.entrypoint;

import com.example.demo.application.usecase.ExternalDataUseCase;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para exponer datos de Alpha Vantage API
 * Separado del UserController para mantener responsabilidades claras
 */
@RestController
@RequestMapping("/api/external")
public class ExternalController {

    private final ExternalDataUseCase externalDataUseCase;

    public ExternalController(ExternalDataUseCase externalDataUseCase) {
        this.externalDataUseCase = externalDataUseCase;
    }

    @GetMapping("/instruments")
    public ResponseEntity<ApiResponse<InstrumentListDTO>> getAllInstruments() {
        try {
            InstrumentListDTO instruments = externalDataUseCase.getAllInstruments();
            return ResponseEntity.ok(new ApiResponse<>(true, "Instrumentos financieros obtenidos exitosamente", instruments));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    // Endpoint de debug para ver la respuesta cruda
    @GetMapping("/instruments/raw")
    public ResponseEntity<ApiResponse<String>> getRawInstruments() {
        try {
            String rawData = externalDataUseCase.getGenericData("function=SYMBOL_SEARCH&keywords=IBM");
            return ResponseEntity.ok(new ApiResponse<>(true, "Respuesta cruda obtenida", rawData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/instruments/{symbol}")
    public ResponseEntity<ApiResponse<SymbolDataDTO>> getSymbolData(@PathVariable String symbol) {
        try {
            SymbolDataDTO symbolData = externalDataUseCase.getSymbolData(symbol);
            return ResponseEntity.ok(new ApiResponse<>(true, "Datos del símbolo " + symbol + " obtenidos exitosamente", symbolData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/data/{endpoint}")
    public ResponseEntity<ApiResponse<String>> getDataFromEndpoint(@PathVariable String endpoint) {
        try {
            String data = externalDataUseCase.getGenericData(endpoint);
            return ResponseEntity.ok(new ApiResponse<>(true, "Datos obtenidos exitosamente", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    // Endpoints adicionales para Alpha Vantage
    
    @GetMapping("/quote/{symbol}")
    public ResponseEntity<ApiResponse<String>> getStockQuote(@PathVariable String symbol) {
        try {
            // Este método requiere agregar el método al use case
            return ResponseEntity.ok(new ApiResponse<>(true, "Funcionalidad próximamente", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/exchange/{from}/{to}")
    public ResponseEntity<ApiResponse<String>> getExchangeRate(@PathVariable String from, @PathVariable String to) {
        try {
            // Este método requiere agregar el método al use case
            return ResponseEntity.ok(new ApiResponse<>(true, "Funcionalidad próximamente", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    // DTO para respuestas consistentes con el resto de la aplicación
    public record ApiResponse<T>(boolean success, String message, T data) {}
}
