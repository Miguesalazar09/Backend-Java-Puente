package com.example.demo.entrypoint;

import com.example.demo.application.usecase.FavoriteService;
import com.example.demo.application.usecase.FavoriteService.FavoriteResult;
import com.example.demo.application.usecase.UserService;
import com.example.demo.domain.model.Favorite;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.ExternalApiPort;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);
    
    // Patrón para validar símbolos bursátiles (letras, números, puntos, guiones)
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Za-z0-9.-]{1,10}$");

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final ExternalApiPort externalApiPort;

    public FavoriteController(FavoriteService favoriteService, UserService userService, ExternalApiPort externalApiPort) {
        this.favoriteService = favoriteService;
        this.userService = userService;
        this.externalApiPort = externalApiPort;
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<AddFavoriteResponse> addFavorite(@PathVariable String symbol, Authentication authentication) {
        try {
            // 1. Validar formato del símbolo
            ValidationResult formatValidation = validateSymbolFormat(symbol);
            if (!formatValidation.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AddFavoriteResponse(false, formatValidation.message(), null));
            }
            
            // 2. Validar que el símbolo existe en Alpha Vantage
            ValidationResult existenceValidation = validateSymbolExists(symbol);
            if (!existenceValidation.isValid()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AddFavoriteResponse(false, existenceValidation.message(), null));
            }
            
            UUID userId = getUserIdFromAuthentication(authentication);
            
            FavoriteResult result = favoriteService.addFavorite(userId, symbol.toUpperCase());
            
            if (result.success()) {
                FavoriteDTO favoriteDTO = result.favorite() != null ? toDTO(result.favorite()) : null;
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AddFavoriteResponse(true, result.message(), favoriteDTO));
            } else {
                HttpStatus status = result.message().contains("ya está en favoritos") ? 
                    HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status)
                    .body(new AddFavoriteResponse(false, result.message(), null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AddFavoriteResponse(false, "Error interno: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<RemoveFavoriteResponse> removeFavorite(@PathVariable String symbol, Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        
        FavoriteResult result = favoriteService.removeFavorite(userId, symbol);
        
        if (result.success()) {
            return ResponseEntity.ok(new RemoveFavoriteResponse(true, result.message()));
        } else {
            HttpStatus status = result.message().contains("no existe") ? 
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new RemoveFavoriteResponse(false, result.message()));
        }
    }

    @GetMapping
    public ResponseEntity<GetFavoritesResponse> getFavorites(Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        
        try {
            List<Favorite> favorites = favoriteService.getUserFavorites(userId);
            List<FavoriteDTO> favoriteDTOs = favorites.stream()
                .map(this::toDTO)
                .toList();
            
            return ResponseEntity.ok(new GetFavoritesResponse(true, "Favoritos obtenidos exitosamente", favoriteDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GetFavoritesResponse(false, "Error al obtener favoritos", null));
        }
    }

    @GetMapping("/{symbol}/check")
    public ResponseEntity<CheckFavoriteResponse> checkFavorite(@PathVariable String symbol, Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        
        boolean isFavorite = favoriteService.isFavorite(userId, symbol);
        
        return ResponseEntity.ok(new CheckFavoriteResponse(true, isFavorite, 
            isFavorite ? "El símbolo está en favoritos" : "El símbolo no está en favoritos"));
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.getUserByEmail(userEmail);
        return user.getId();
    }

    private FavoriteDTO toDTO(Favorite favorite) {
        return new FavoriteDTO(favorite.getSymbol(), favorite.getCreatedAt().toString());
    }

    /**
     * Valida el formato del símbolo bursátil
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

    /**
     * Valida que el símbolo existe en Alpha Vantage
     */
    private ValidationResult validateSymbolExists(String symbol) {
        try {
            String normalizedSymbol = symbol.trim().toUpperCase();
            
            // Buscar el símbolo específico en Alpha Vantage
            InstrumentListDTO result = externalApiPort.searchSymbol(normalizedSymbol);
            
            if (result == null || result.bestMatches() == null || result.bestMatches().isEmpty()) {
                logger.warn("No se encontraron resultados en Alpha Vantage para el símbolo: {}", normalizedSymbol);
                return new ValidationResult(false, "El símbolo '" + normalizedSymbol + "' no existe en Alpha Vantage");
            }
            
            // Verificar si el símbolo exacto existe en los resultados
            boolean symbolExists = result.bestMatches().stream()
                .anyMatch(instrument -> normalizedSymbol.equals(instrument.symbol()));
            
            if (symbolExists) {
                logger.info("Símbolo {} validado exitosamente en Alpha Vantage", normalizedSymbol);
                return new ValidationResult(true, "Símbolo válido encontrado en Alpha Vantage");
            } else {
                // Si no hay coincidencia exacta, revisar si hay coincidencias parciales
                boolean hasPartialMatch = result.bestMatches().stream()
                    .anyMatch(instrument -> instrument.symbol().contains(normalizedSymbol) || 
                                          normalizedSymbol.contains(instrument.symbol()));
                
                if (hasPartialMatch) {
                    logger.warn("Símbolo {} tiene coincidencias parciales pero no exactas en Alpha Vantage", normalizedSymbol);
                    return new ValidationResult(false, "El símbolo '" + normalizedSymbol + "' no coincide exactamente con ningún instrumento disponible");
                } else {
                    return new ValidationResult(false, "El símbolo '" + normalizedSymbol + "' no existe en Alpha Vantage");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al validar símbolo en Alpha Vantage: {}", symbol, e);
            return new ValidationResult(false, "Error al validar el símbolo con Alpha Vantage: " + e.getMessage());
        }
    }

    // Records para validación y DTOs
    record ValidationResult(boolean isValid, String message) {}
    
    // DTOs
    record AddFavoriteResponse(boolean success, String message, FavoriteDTO favorite) {}
    record RemoveFavoriteResponse(boolean success, String message) {}
    record GetFavoritesResponse(boolean success, String message, List<FavoriteDTO> favorites) {}
    record CheckFavoriteResponse(boolean success, boolean isFavorite, String message) {}
    record FavoriteDTO(String symbol, String createdAt) {}
}
