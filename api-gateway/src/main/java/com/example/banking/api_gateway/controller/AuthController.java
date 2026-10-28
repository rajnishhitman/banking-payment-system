package com.example.banking.api_gateway.controller;

import java.time.Instant;
import java.util.Map;

import com.example.banking.api_gateway.dto.LoginRequest;
import com.example.banking.api_gateway.dto.LoginResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    public AuthController(
        ReactiveAuthenticationManager authenticationManager,
        JwtEncoder jwtEncoder,
        @Value("${security.jwt.expiration-seconds}")
        long expirationSeconds) {

        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(
        @RequestBody LoginRequest request) {

        var authenticationRequest =
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password());

        return authenticationManager.authenticate(authenticationRequest)
            .map(this::generateToken)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .onErrorResume(
                AuthenticationException.class,
                exception -> Mono.just(
                    ResponseEntity.status(
                            HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "error",
                            "Invalid username or password"))));
    }

    private LoginResponse generateToken(
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

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(headers, claims))
            .getTokenValue();

        return new LoginResponse(
            token,
            "Bearer",
            expirationSeconds);
    }
}
