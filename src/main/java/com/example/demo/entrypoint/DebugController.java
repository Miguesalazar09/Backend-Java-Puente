package com.example.demo.entrypoint;

import com.example.demo.domain.port.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user/{email}")
    public String checkUser(@PathVariable String email) {
        var user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return String.format("Email: %s, Hash: %s, Role: %s", 
                user.get().getEmail(), 
                user.get().getPass(), 
                user.get().getRole());
        }
        return "Usuario no encontrado";
    }
    
    @PostMapping("/check-password")
    public String checkPassword(@RequestParam String email, @RequestParam String password) {
        var user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            boolean matches = passwordEncoder.matches(password, user.get().getPass());
            return String.format("Email: %s, Password matches: %s, Hash: %s", 
                email, matches, user.get().getPass());
        }
        return "Usuario no encontrado";
    }
    
    @PostMapping("/generate-hash")
    public String generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return String.format("Password: %s, Hash: %s", password, hash);
    }
}
