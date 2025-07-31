package com.example.demo.infrastructure.config;

import com.example.demo.domain.port.UserRepository;
import com.example.demo.infrastructure.security.CustomUserDetailsService;
import com.example.demo.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                         JwtAuthenticationFilter jwtAuthFilter,
                                         AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/debug/**").permitAll() // Endpoint temporal para debug
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Permitir registro sin autenticación
                .requestMatchers(HttpMethod.PUT, "/api/users/profile").authenticated() // Cualquier usuario autenticado puede actualizar su perfil
                .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated() // Cualquier usuario autenticado puede ver su perfil
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN") // Solo admins pueden ver la lista
                .requestMatchers("/api/users/admin").permitAll() // Permitir registro de admin por ahora
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Solo admins pueden acceder
                .requestMatchers("/api/external/**").authenticated() // Endpoints de APIs externas requieren autenticación
                .requestMatchers("/api/favorites/**").authenticated() // Endpoints de favoritos requieren autenticación
                .anyRequest().authenticated() // Cambiado de .permitAll() a .authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, 
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }
}
