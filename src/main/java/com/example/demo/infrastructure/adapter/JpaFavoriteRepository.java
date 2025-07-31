package com.example.demo.infrastructure.adapter;

import com.example.demo.domain.model.Favorite;
import com.example.demo.domain.port.FavoriteRepository;
import com.example.demo.infrastructure.entity.FavoriteEntity;
import com.example.demo.infrastructure.repository.SpringDataFavoriteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaFavoriteRepository implements FavoriteRepository {

    private final SpringDataFavoriteRepository springRepo;

    public JpaFavoriteRepository(SpringDataFavoriteRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Favorite save(Favorite favorite) {
        FavoriteEntity entity = toEntity(favorite);
        FavoriteEntity savedEntity = springRepo.save(entity);
        return toModel(savedEntity);
    }

    @Override
    public Optional<Favorite> findByUserIdAndSymbol(UUID userId, String symbol) {
        return springRepo.findByUserIdAndSymbol(userId, symbol)
                .map(this::toModel);
    }

    @Override
    public List<Favorite> findByUserId(UUID userId) {
        return springRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByUserIdAndSymbol(UUID userId, String symbol) {
        springRepo.deleteByUserIdAndSymbol(userId, symbol);
    }

    @Override
    public boolean existsByUserIdAndSymbol(UUID userId, String symbol) {
        return springRepo.existsByUserIdAndSymbol(userId, symbol);
    }

    private Favorite toModel(FavoriteEntity entity) {
        return new Favorite(entity.getId(), entity.getUserId(), entity.getSymbol(), entity.getCreatedAt());
    }

    private FavoriteEntity toEntity(Favorite favorite) {
        return new FavoriteEntity(favorite.getId(), favorite.getUserId(), favorite.getSymbol(), favorite.getCreatedAt());
    }
}
