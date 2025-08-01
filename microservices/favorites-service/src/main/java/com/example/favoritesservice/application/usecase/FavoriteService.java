package com.example.favoritesservice.application.usecase;

import com.example.favoritesservice.domain.model.Favorite;
import com.example.favoritesservice.domain.port.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    
    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }
    
    public FavoriteResult addFavorite(UUID userId, String symbol) {
        try {
            // Verificar si ya existe
            if (favoriteRepository.existsByUserIdAndSymbol(userId, symbol)) {
                return new FavoriteResult(false, "El símbolo " + symbol + " ya está en favoritos", null);
            }
            
            Favorite favorite = new Favorite(userId, symbol);
            Favorite saved = favoriteRepository.save(favorite);
            
            return new FavoriteResult(true, "Favorito agregado exitosamente", saved);
        } catch (Exception e) {
            return new FavoriteResult(false, "Error al agregar favorito: " + e.getMessage(), null);
        }
    }
    
    public FavoriteResult removeFavorite(UUID userId, String symbol) {
        try {
            if (!favoriteRepository.existsByUserIdAndSymbol(userId, symbol)) {
                return new FavoriteResult(false, "El símbolo " + symbol + " no existe en favoritos", null);
            }
            
            favoriteRepository.deleteByUserIdAndSymbol(userId, symbol);
            return new FavoriteResult(true, "Favorito eliminado exitosamente", null);
        } catch (Exception e) {
            return new FavoriteResult(false, "Error al eliminar favorito: " + e.getMessage(), null);
        }
    }
    
    public List<Favorite> getUserFavorites(UUID userId) {
        return favoriteRepository.findByUserId(userId);
    }
    
    public record FavoriteResult(boolean success, String message, Favorite favorite) {}
}
