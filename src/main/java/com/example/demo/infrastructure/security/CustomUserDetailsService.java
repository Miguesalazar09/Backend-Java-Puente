package com.example.demo.infrastructure.security;

import com.example.demo.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;

public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public CustomUserDetailsService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Intentando cargar usuario: {}", username);
        
        // Buscar usuario en la base de datos
        com.example.demo.domain.model.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
        
        logger.info("Usuario encontrado: {}, rol: {}", user.getEmail(), user.getRole());
        logger.debug("Hash de contraseña desde DB: {}", user.getPass());
        
        // Convertir el rol del dominio a Spring Security Authority
        String authority = "ROLE_" + user.getRole().name();
        logger.debug("Authority asignada: {}", authority);
        
        UserDetails userDetails = User.builder()
                .username(user.getEmail())
                .password(user.getPass()) // La contraseña ya está hasheada en la base de datos
                .authorities(authority)
                .build();
                
        logger.info("UserDetails creado exitosamente para: {}", username);
        return userDetails;
    }

    /**
     * Método para encriptar contraseñas antes de guardarlas en la base de datos
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
