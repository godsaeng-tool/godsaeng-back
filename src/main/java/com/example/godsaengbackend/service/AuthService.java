package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.UserDto;
import com.example.godsaengbackend.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthService(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public UserDto.LoginResponse login(UserDto.LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());
        
        if (!userService.verifyPassword(user, request.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        
        String accessToken = tokenService.createAccessToken(user.getEmail());
        String refreshToken = tokenService.createRefreshToken(user.getEmail());
        
        return UserDto.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .role(user.getRole())
            .isGodMode(user.getIsGodMode())
            .build();
    }
} 