package com.example.banking.api_gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.banking.api_gateway.dto.LoginRequest;
import com.example.banking.api_gateway.dto.LoginResponse;

import com.example.banking.api_gateway.dto.RefreshRequest;
import com.example.banking.api_gateway.security.RefreshTokenService;
import com.example.banking.api_gateway.security.RefreshTokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthController(
        ReactiveAuthenticationManager authenticationManager,
        JwtEncoder jwtEncoder,
        @Value("${security.jwt.expiration-seconds}")
        long expirationSeconds, RefreshTokenService refreshTokenService,
        RefreshTokenStore refreshTokenStore) {

        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(
        @RequestBody LoginRequest request) {

        Authentication authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.username(),
                request.password()
            );

        return authenticationManager.authenticate(authenticationRequest)
            .<ResponseEntity<?>>flatMap(authentication -> {

                String accessToken = generateAccessToken(authentication);

                String refreshToken = refreshTokenService.generateRefreshToken();

                return refreshTokenStore.save(
                        refreshToken,
                        authentication.getName()
                    )
                    .map(saved -> {
                        if (!saved) {
                            throw new IllegalStateException(
                                "Unable to save refresh token"
                            );
                        }

                        LoginResponse response = new LoginResponse(
                            accessToken,
                            refreshToken,
                            "Bearer",
                            expirationSeconds
                        );

                        return ResponseEntity.ok(response);
                    });
            })
            .onErrorResume(AuthenticationException.class,
                exception -> Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "error",
                            "Invalid username or password"
                        ))
                ));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<?>> refresh(
        @RequestBody RefreshRequest request) {

        String oldRefreshToken = request.refreshToken();

        return refreshTokenStore.findUsername(oldRefreshToken)
            .<ResponseEntity<?>>flatMap(username -> {

                // Invalidate old refresh token
                return refreshTokenStore.delete(oldRefreshToken)
                    .flatMap(deleted -> {

                        if (!deleted) {
                            return Mono.empty();
                        }

                        // Generate new access token
                        Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                        String newAccessToken =
                            generateAccessToken(authentication);

                        // Generate new refresh token
                        String newRefreshToken =
                            refreshTokenService.generateRefreshToken();

                        // Store new refresh token
                        return refreshTokenStore.save(
                                newRefreshToken,
                                username
                            )
                            .map(saved -> {
                                if (!saved) {
                                    throw new IllegalStateException(
                                        "Unable to save refresh token"
                                    );
                                }

                                LoginResponse response = new LoginResponse(
                                    newAccessToken,
                                    newRefreshToken,
                                    "Bearer",
                                    expirationSeconds
                                );

                                return ResponseEntity.ok(response);
                            });
                    });
            })
            .switchIfEmpty(Mono.just(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error",
                        "Invalid or expired refresh token"
                    ))
            ));
    }

    private String generateAccessToken(
        Authentication authentication) {

        Instant now = Instant.now();

        var claims = JwtClaimsSet.builder()
            .subject(authentication.getName())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .claim("roles", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
            .build();

        var headers = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(headers, claims))
            .getTokenValue();
    }
}
