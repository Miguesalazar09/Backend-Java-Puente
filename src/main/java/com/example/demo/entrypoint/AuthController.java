package com.example.demo.entrypoint;

import com.example.demo.application.usecase.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthResponse> authenticate(
            @RequestBody AuthService.AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
