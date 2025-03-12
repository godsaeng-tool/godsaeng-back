package com.example.godsaengbackend.service;

import com.example.godsaengbackend.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createAccessToken(String email) {
        return jwtTokenProvider.createAccessToken(email);
    }
    
    public String createRefreshToken(String email) {
        return jwtTokenProvider.createRefreshToken(email);
    }
    
    public String refreshToken(String refreshToken) {
        return jwtTokenProvider.refreshToken(refreshToken);
    }
} 