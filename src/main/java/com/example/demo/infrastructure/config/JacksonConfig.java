package com.example.demo.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configurar para que strings vacíos se conviertan a null para todos los enums
        mapper.coercionConfigDefaults()
               .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        
        // También configurar para que strings en blanco se conviertan a null
        mapper.coercionConfigDefaults()
               .setCoercion(CoercionInputShape.EmptyArray, CoercionAction.AsNull);
               
        return mapper;
    }
}
