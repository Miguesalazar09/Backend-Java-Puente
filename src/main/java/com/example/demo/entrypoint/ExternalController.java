package com.example.demo.entrypoint;

import com.example.demo.application.usecase.ExternalDataUseCase;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import com.example.demo.infrastructure.external.dto.SymbolDataDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * Controller para exponer datos de Alpha Vantage API
 * Separado del UserController para mantener responsabilidades claras
 */
@RestController
@RequestMapping("/api/external")
public class ExternalController {

    // Patrón para validar símbolos bursátiles (letras, números, puntos, guiones)
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Za-z0-9.-]{1,10}$");

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
        // Validar formato del símbolo
        ValidationResult formatValidation = validateSymbolFormat(symbol);
        if (!formatValidation.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, formatValidation.message(), null));
        }
        
        try {
            SymbolDataDTO symbolData = externalDataUseCase.getSymbolData(symbol.toUpperCase());
            return ResponseEntity.ok(new ApiResponse<>(true, "Datos del símbolo " + symbol.toUpperCase() + " obtenidos exitosamente", symbolData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error al obtener datos para símbolo " + symbol.toUpperCase() + ": " + e.getMessage(), null));
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

    /**
     * Valida el formato del símbolo bursátil
     * Mismas reglas que en FavoriteController
     */
    private ValidationResult validateSymbolFormat(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ValidationResult(false, "El símbolo no puede estar vacío");
        }
        
        String trimmedSymbol = symbol.trim();
        if (!SYMBOL_PATTERN.matcher(trimmedSymbol).matches()) {
            return new ValidationResult(false, "Formato de símbolo inválido. Debe contener solo letras, números, puntos y guiones (máximo 10 caracteres)");
        }
        
        return new ValidationResult(true, "Formato válido");
    }

    // Records para validación y respuesta
    record ValidationResult(boolean isValid, String message) {}

    // DTO para respuestas consistentes con el resto de la aplicación
    public record ApiResponse<T>(boolean success, String message, T data) {}
}
