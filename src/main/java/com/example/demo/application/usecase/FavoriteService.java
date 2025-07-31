package com.example.demo.application.usecase;

import com.example.demo.domain.model.Favorite;
import com.example.demo.domain.port.FavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public FavoriteResult addFavorite(UUID userId, String symbol) {
        // Validar que el símbolo no esté vacío
        if (symbol == null || symbol.trim().isEmpty()) {
            return new FavoriteResult(false, "El símbolo no puede estar vacío", null);
        }

        // Normalizar el símbolo (convertir a mayúsculas)
        String normalizedSymbol = symbol.trim().toUpperCase();

        // Verificar si ya existe el favorito
        if (favoriteRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
            return new FavoriteResult(false, "El símbolo ya está en favoritos", null);
        }

        try {
            Favorite favorite = new Favorite(userId, normalizedSymbol);
            Favorite savedFavorite = favoriteRepository.save(favorite);
            logger.info("Favorito añadido: usuario {} - símbolo {}", userId, normalizedSymbol);
            return new FavoriteResult(true, "Favorito añadido exitosamente", savedFavorite);
        } catch (Exception e) {
            logger.error("Error al añadir favorito: usuario {} - símbolo {}", userId, normalizedSymbol, e);
            return new FavoriteResult(false, "Error interno al añadir favorito", null);
        }
    }

    public FavoriteResult removeFavorite(UUID userId, String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new FavoriteResult(false, "El símbolo no puede estar vacío", null);
        }

        String normalizedSymbol = symbol.trim().toUpperCase();

        // Verificar si existe el favorito
        if (!favoriteRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
            return new FavoriteResult(false, "El favorito no existe", null);
        }

        try {
            favoriteRepository.deleteByUserIdAndSymbol(userId, normalizedSymbol);
            logger.info("Favorito eliminado: usuario {} - símbolo {}", userId, normalizedSymbol);
            return new FavoriteResult(true, "Favorito eliminado exitosamente", null);
        } catch (Exception e) {
            logger.error("Error al eliminar favorito: usuario {} - símbolo {}", userId, normalizedSymbol, e);
            return new FavoriteResult(false, "Error interno al eliminar favorito", null);
        }
    }

    public List<Favorite> getUserFavorites(UUID userId) {
        try {
            return favoriteRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Error al obtener favoritos del usuario {}", userId, e);
            throw new RuntimeException("Error al obtener favoritos", e);
        }
    }

    public boolean isFavorite(UUID userId, String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        return favoriteRepository.existsByUserIdAndSymbol(userId, normalizedSymbol);
    }

    // Record para el resultado de las operaciones
    public record FavoriteResult(boolean success, String message, Favorite favorite) {}
}
