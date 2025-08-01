package com.example.favoritesservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://user-service:8081")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}/exists")
    boolean userExists(@PathVariable("id") String userId);
    
    @GetMapping("/api/users/email/{email}/id")
    java.util.UUID getUserIdByEmail(@PathVariable("email") String email);
}
