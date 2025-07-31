package com.example.demo.infrastructure.repository;

import com.example.demo.infrastructure.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {
    Optional<FavoriteEntity> findByUserIdAndSymbol(UUID userId, String symbol);
    List<FavoriteEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserIdAndSymbol(UUID userId, String symbol);
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);
}
