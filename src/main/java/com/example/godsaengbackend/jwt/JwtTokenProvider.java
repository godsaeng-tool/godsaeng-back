package com.example.godsaengbackend.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long accessTokenValidity,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidity,
            @Lazy UserDetailsService userDetailsService) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.userDetailsService = userDetailsService;
    }

    public String createAccessToken(String username) {
        return createToken(username, accessTokenValidity, "ACCESS");
    }

    public String createRefreshToken(String username) {
        return createToken(username, refreshTokenValidity, "REFRESH");
    }

    private String createToken(String username, long validityInMillis, String tokenType) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("type", tokenType);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
                    
            // 토큰 타입 검증 (ACCESS 토큰만 API 접근 허용)
            String tokenType = claims.getBody().get("type", String.class);
            if (!"ACCESS".equals(tokenType)) {
                logger.warn("Invalid token type: {}", tokenType);
                return false;
            }
            
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.warn("유효하지 않은 JWT 서명: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT 토큰 처리 중 오류: {}", e.getMessage());
            return false;
        }
    }

    public String refreshToken(String refreshToken) {
        try {
            // Refresh 토큰 검증
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken);
                    
            // Refresh 토큰 타입 검증
            String tokenType = claims.getBody().get("type", String.class);
            if (!"REFRESH".equals(tokenType)) {
                throw new JwtException("Invalid token type");
            }
            
            // 만료 시간 검증
            if (claims.getBody().getExpiration().before(new Date())) {
                throw new JwtException("Refresh token has expired");
            }
            
            String username = getUsername(refreshToken);
            return createAccessToken(username);
        } catch (Exception e) {
            logger.error("Refresh token 처리 중 오류: {}", e.getMessage());
            throw new JwtException("Invalid refresh token: " + e.getMessage());
        }
    }
}
