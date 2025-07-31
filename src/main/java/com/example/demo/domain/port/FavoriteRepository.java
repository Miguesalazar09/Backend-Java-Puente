package com.example.demo.domain.port;

import com.example.demo.domain.model.Favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {
    Favorite save(Favorite favorite);
    Optional<Favorite> findByUserIdAndSymbol(UUID userId, String symbol);
    List<Favorite> findByUserId(UUID userId);
    void deleteByUserIdAndSymbol(UUID userId, String symbol);
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);
}
