package com.example.favoritesservice.infrastructure.persistence;

import com.example.favoritesservice.domain.model.Favorite;
import com.example.favoritesservice.domain.port.FavoriteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaFavoriteRepository extends JpaRepository<Favorite, UUID>, FavoriteRepository {
    
    List<Favorite> findByUserId(UUID userId);
    
    Optional<Favorite> findByUserIdAndSymbol(UUID userId, String symbol);
    
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);
    
    void deleteByUserIdAndSymbol(UUID userId, String symbol);
}
