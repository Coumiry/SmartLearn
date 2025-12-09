package com.smartlearn.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private SecretKey key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        // secret 需要足够长，不然会报错
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     */
    public String generateToken(String userId, String username, String role) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + properties.getExpireMillis());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(exp)
                .addClaims(Map.of(
                        "uid",userId,
                        "username", username,
                        "role", role
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token，非法/过期都会抛异常
     */
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String getUserId(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.get("uid", String.class);
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.get("username", String.class);
    }

    public String getRole(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.get("role", String.class);
    }
}
