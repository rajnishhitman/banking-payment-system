package com.example.banking.api_gateway.dto;

public record LoginRequest(
    String username,
    String password
) {
}
