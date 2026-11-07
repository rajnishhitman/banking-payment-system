package com.example.banking.api_gateway.security;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh_token:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RefreshTokenProperties refreshTokenProperties;

    public RefreshTokenStore(
        ReactiveStringRedisTemplate redisTemplate,
        RefreshTokenProperties refreshTokenProperties) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    public Mono<Boolean> save(String refreshToken, String username) {

        String key = KEY_PREFIX + hashToken(refreshToken);

        return redisTemplate.opsForValue()
            .set(
                key,
                username,
                Duration.ofSeconds(refreshTokenProperties.expirationSeconds())
            );
    }

    public Mono<String> findUsername(String refreshToken) {

        String key = KEY_PREFIX + hashToken(refreshToken);

        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> delete(String refreshToken) {

        String key = KEY_PREFIX + hashToken(refreshToken);

        return redisTemplate.delete(key)
            .map(deletedCount -> deletedCount > 0);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
