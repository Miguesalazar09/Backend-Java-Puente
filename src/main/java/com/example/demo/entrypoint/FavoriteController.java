package com.example.demo.entrypoint;

import com.example.demo.application.usecase.FavoriteService;
import com.example.demo.application.usecase.FavoriteService.FavoriteResult;
import com.example.demo.application.usecase.UserService;
import com.example.demo.domain.model.Favorite;
import com.example.demo.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<AddFavoriteResponse> addFavorite(@PathVariable String symbol, Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        
        FavoriteResult result = favoriteService.addFavorite(userId, symbol);
        
        if (result.success()) {
            FavoriteDTO favoriteDTO = result.favorite() != null ? toDTO(result.favorite()) : null;
            return ResponseEntity.ok(new AddFavoriteResponse(true, result.message(), favoriteDTO));
        } else {
            HttpStatus status = result.message().contains("ya está en favoritos") ? 
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new AddFavoriteResponse(false, result.message(), null));
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

    // DTOs
    record AddFavoriteResponse(boolean success, String message, FavoriteDTO favorite) {}
    record RemoveFavoriteResponse(boolean success, String message) {}
    record GetFavoritesResponse(boolean success, String message, List<FavoriteDTO> favorites) {}
    record CheckFavoriteResponse(boolean success, boolean isFavorite, String message) {}
    record FavoriteDTO(String symbol, String createdAt) {}
}
