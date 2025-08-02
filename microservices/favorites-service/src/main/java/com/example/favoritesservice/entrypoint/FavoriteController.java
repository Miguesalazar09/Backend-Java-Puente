package com.example.favoritesservice.entrypoint;

import com.example.favoritesservice.application.usecase.FavoriteService;
import com.example.favoritesservice.application.usecase.FavoriteService.FavoriteResult;
import com.example.favoritesservice.domain.model.Favorite;
import com.example.favoritesservice.infrastructure.client.ExternalServiceClient;
import com.example.favoritesservice.infrastructure.client.UserServiceClient;
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
    private final UserServiceClient userServiceClient;
    private final ExternalServiceClient externalServiceClient;

    public FavoriteController(FavoriteService favoriteService, UserServiceClient userServiceClient, ExternalServiceClient externalServiceClient) {
        this.favoriteService = favoriteService;
        this.userServiceClient = userServiceClient;
        this.externalServiceClient = externalServiceClient;
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
            
            // 2. Validar que el símbolo existe en Alpha Vantage (llamada al external-service)
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

    @DeleteMapping("/{id}")
    public ResponseEntity<RemoveFavoriteResponse> removeFavorite(@PathVariable UUID id, Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            
            FavoriteResult result = favoriteService.removeFavoriteById(userId, id);
            
            if (result.success()) {
                return ResponseEntity.ok(new RemoveFavoriteResponse(true, result.message()));
            } else {
                // Determinar el código de estado basado en el mensaje
                HttpStatus status;
                if (result.message().contains("no existe")) {
                    status = HttpStatus.NOT_FOUND;
                } else if (result.message().contains("No tienes permisos")) {
                    status = HttpStatus.FORBIDDEN;
                } else {
                    status = HttpStatus.BAD_REQUEST;
                }
                
                return ResponseEntity.status(status)
                    .body(new RemoveFavoriteResponse(false, result.message()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RemoveFavoriteResponse(false, "ID de favorito inválido: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RemoveFavoriteResponse(false, "Error interno: " + e.getMessage()));
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
                .body(new GetFavoritesResponse(false, "Error interno: " + e.getMessage(), null));
        }
    }

    private ValidationResult validateSymbolFormat(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ValidationResult(false, "El símbolo no puede estar vacío");
        }
        
        String trimmedSymbol = symbol.trim();
        if (!SYMBOL_PATTERN.matcher(trimmedSymbol).matches()) {
            return new ValidationResult(false, "Formato de símbolo inválido. Solo se permiten letras, números, puntos y guiones (máximo 10 caracteres)");
        }
        
        return new ValidationResult(true, "Formato válido");
    }

    private ValidationResult validateSymbolExists(String symbol) {
        try {
            // Llamada al external-service para validar que el símbolo existe
            boolean exists = externalServiceClient.validateSymbol(symbol);
            if (!exists) {
                return new ValidationResult(false, "El símbolo " + symbol + " no es válido o no existe en el mercado");
            }
            return new ValidationResult(true, "Símbolo válido");
        } catch (Exception e) {
            logger.error("Error validating symbol {}: {}", symbol, e.getMessage());
            return new ValidationResult(false, "Error al validar el símbolo: " + e.getMessage());
        }
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        try {
            // Obtener información del usuario desde user-service
            return userServiceClient.getUserIdByEmail(authentication.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo información del usuario: " + e.getMessage());
        }
    }

    private FavoriteDTO toDTO(Favorite favorite) {
        return new FavoriteDTO(favorite.getId(), favorite.getUserId(), favorite.getSymbol());
    }

    // DTOs y Response classes
    public record FavoriteDTO(UUID id, UUID userId, String symbol) {}
    public record AddFavoriteResponse(boolean success, String message, FavoriteDTO favorite) {}
    public record RemoveFavoriteResponse(boolean success, String message) {}
    public record GetFavoritesResponse(boolean success, String message, List<FavoriteDTO> favorites) {}
    public record ValidationResult(boolean isValid, String message) {}

    // Exception handler for authentication errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("Usuario no autenticado")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Authentication required", "User must be authenticated to access this resource"));
        }
        // For other runtime exceptions, return 500
        logger.error("Internal server error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal server error", e.getMessage()));
    }

    // DTOs and Response classes
    public record ErrorResponse(String error, String message) {}
}
