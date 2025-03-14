package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.UserDto;
import com.example.godsaengbackend.service.AuthService;
import com.example.godsaengbackend.service.TokenService;
import com.example.godsaengbackend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final TokenService tokenService;

    public UserController(UserService userService, AuthService authService, TokenService tokenService) {
        this.userService = userService;
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto.UserResponse> signup(@Valid @RequestBody UserDto.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(
            @Valid @RequestBody UserDto.LoginRequest request,
            HttpServletResponse response) {
        
        UserDto.LoginResponse loginResponse = authService.login(request);
        
        // Refresh Token을 HTTP-only 쿠키로 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
        refreshTokenCookie.setPath("/api/users/refresh"); // 특정 경로에서만 사용 가능
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshTokenCookie);
        
        // refreshToken은 @JsonProperty 설정으로 자동으로 응답에서 제외됨
        
        return ResponseEntity.ok(loginResponse);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        // 쿠키에서 Refresh Token을 가져오거나, 요청 본문에서 가져옴
        String refreshToken = refreshTokenCookie;
        if (refreshToken == null && requestBody != null) {
            refreshToken = requestBody.get("refreshToken");
        }
        
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Refresh token is missing"));
        }
        
        try {
            String newAccessToken = tokenService.refreshToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid refresh token", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserResponse> getCurrentUser(@RequestAttribute("email") String email) {
        return ResponseEntity.ok(userService.getUserProfile(email));
    }
    
    // 갓생 모드 업데이트 엔드포인트 추가
    @PutMapping("/god-mode")
    public ResponseEntity<UserDto.UserResponse> updateGodMode(
            @RequestAttribute("email") String email,
            @RequestBody UserDto.GodModeRequest request) {
        return ResponseEntity.ok(userService.updateGodMode(email, request.getIsGodMode()));
    }
}
