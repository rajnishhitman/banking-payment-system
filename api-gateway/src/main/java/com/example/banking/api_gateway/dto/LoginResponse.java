package com.example.banking.api_gateway.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
