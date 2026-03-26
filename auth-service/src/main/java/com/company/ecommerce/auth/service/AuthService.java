package com.company.ecommerce.auth.service;

import com.company.ecommerce.auth.dto.*;
import com.company.ecommerce.auth.entity.RefreshToken;
import com.company.ecommerce.auth.entity.Role;
import com.company.ecommerce.auth.entity.User;
import com.company.ecommerce.auth.repository.RefreshTokenRepository;
import com.company.ecommerce.auth.repository.RoleRepository;
import com.company.ecommerce.auth.repository.UserRepository;
import com.company.ecommerce.auth.security.CustomUserDetails;
import com.company.ecommerce.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()->new RuntimeException("Role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(true)
                .build();
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = generateRefreshToken(userDetails.getUserId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateRefreshToken(Long userId){
        String token = UUID.randomUUID().toString();

        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now()
                        .plus(Duration.ofMillis(refreshTokenExpiration)))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public AuthResponse refreshToken(RefreshTokenRequest request){
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(()->new RuntimeException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            throw new RuntimeException("Refresh token revoked");
        }

        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }

    public void logout(LogoutRequest request){
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(()->new RuntimeException("Invalid refresh token"));
        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}
