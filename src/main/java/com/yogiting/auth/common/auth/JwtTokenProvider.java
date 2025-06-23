package com.yogiting.auth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final int expiration;
    private final Key SECRET_KEY;
    private final String secretKeyRt;
    private final int expirationRt;
    private final Key SECRET_KEY_RT;

    private final RedisTemplate<String, Object> redisTemplate;


    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration, @Value("${jwt.secretKeyRt}") String secretKeyRt, @Value("${jwt.expirationRt}") int expirationRt, RedisTemplate<String, Object> redisTemplate) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
        this.secretKeyRt = secretKeyRt;
        this.expirationRt = expirationRt;
        this.SECRET_KEY_RT = new SecretKeySpec(Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
        this.redisTemplate = redisTemplate;
    }

    public String createToken(String email, Long memberId) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", memberId);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(SECRET_KEY)
                .compact();

        return token;
    }

    public String createRefreshToken(String email, Long memberId) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", memberId);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(SECRET_KEY_RT)
                .compact();

        redisTemplate.opsForValue().set(email, token, expirationRt, TimeUnit.MINUTES);

        return token;
    }

    public String createTokenWithRefreshToken(String refreshToken) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Object rt = redisTemplate.opsForValue().get(claims.getSubject());
        if (rt == null || !rt.toString().equals(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return createToken(claims.getSubject(), Long.parseLong(claims.getId()));
    }
}
