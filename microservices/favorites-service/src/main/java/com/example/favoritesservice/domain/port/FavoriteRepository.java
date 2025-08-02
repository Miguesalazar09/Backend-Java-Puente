package com.example.favoritesservice.domain.port;

import com.example.favoritesservice.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {
    Favorite save(Favorite favorite);
    Optional<Favorite> findByUserIdAndSymbol(UUID userId, String symbol);
    Optional<Favorite> findById(UUID id);
    List<Favorite> findByUserId(UUID userId);
    void deleteByUserIdAndSymbol(UUID userId, String symbol);
    void deleteById(UUID id);
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
