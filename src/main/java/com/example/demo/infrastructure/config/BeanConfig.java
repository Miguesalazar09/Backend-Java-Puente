package com.example.demo.infrastructure.config;

import com.example.demo.application.usecase.UserService;
import com.example.demo.domain.port.UserRepository;
import com.example.demo.infrastructure.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UserService userService(UserRepository userRepository, CustomUserDetailsService userDetailsService) {
        return new UserService(userRepository, userDetailsService);
    }
}
