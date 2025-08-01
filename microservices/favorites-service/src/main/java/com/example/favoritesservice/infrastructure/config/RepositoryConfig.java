package com.example.favoritesservice.infrastructure.config;

import com.example.favoritesservice.domain.port.FavoriteRepository;
import com.example.favoritesservice.infrastructure.persistence.JpaFavoriteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RepositoryConfig {

    @Bean
    @Primary
    public FavoriteRepository favoriteRepository(JpaFavoriteRepository jpaFavoriteRepository) {
        return jpaFavoriteRepository;
    }
}
