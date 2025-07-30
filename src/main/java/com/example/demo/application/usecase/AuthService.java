package com.example.demo.application.usecase;

import com.example.demo.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public AuthResponse authenticate(AuthRequest request) {
        logger.info("Iniciando autenticación para usuario: {}", request.email());
        
        // Validaciones
        if (request.password() == null || request.password().trim().isEmpty()) {
            logger.error("Password vacío para usuario: {}", request.email());
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            logger.error("Email vacío");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        try {
            logger.debug("Ejecutando autenticación con AuthenticationManager...");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            logger.info("Autenticación exitosa para usuario: {}", request.email());
            
            // Cargar los detalles del usuario real desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
            logger.debug("UserDetails cargado: {}", userDetails.getUsername());
            
            String jwtToken = jwtService.generateToken(userDetails);
            logger.info("Token JWT generado exitosamente para usuario: {}", request.email());
            
            return new AuthResponse(jwtToken);
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            logger.error("Credenciales incorrectas para usuario: {}", request.email());
            throw e; // Propagar para que GlobalExceptionHandler la maneje
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.error("Error de autenticación para usuario {}: {}", request.email(), e.getMessage());
            throw e; // Propagar para que GlobalExceptionHandler la maneje
        } catch (Exception e) {
            logger.error("Error inesperado durante la autenticación para usuario {}: {}", request.email(), e.getMessage());
            throw new RuntimeException("Authentication service error", e);
        }
    }

    public record AuthRequest(String email, String password) {}
    public record AuthResponse(String token) {}
}
