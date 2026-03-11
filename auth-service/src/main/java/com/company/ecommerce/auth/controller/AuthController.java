package com.company.ecommerce.auth.controller;

import com.company.ecommerce.auth.dto.AuthResponse;
import com.company.ecommerce.auth.dto.LoginRequest;
import com.company.ecommerce.auth.dto.RefreshTokenRequest;
import com.company.ecommerce.auth.dto.RegisterRequest;
import com.company.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok("user registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        authService.login(request);
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndPoint(){
        return ResponseEntity.ok("You accessed protected endpoint");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
            ){
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
