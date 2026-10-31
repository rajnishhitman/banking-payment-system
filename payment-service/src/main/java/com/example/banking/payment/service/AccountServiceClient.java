package com.example.banking.payment.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountServiceClient {

    private final RestClient accountClient;

    public AccountServiceClient(RestClient.Builder builder) {
        this.accountClient = builder
            .baseUrl("http://localhost:8081")
            .build();
    }

    @CircuitBreaker(name = "accountService")
    public void debit(
        Long accountId,
        TransferService.AccountOperationRequest request) {

        accountClient.post()
            .uri("/api/accounts/{id}/debit", accountId)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }

    @CircuitBreaker(name = "accountService")
    public void credit(
        Long accountId,
        TransferService.AccountOperationRequest request) {

        accountClient.post()
            .uri("/api/accounts/{id}/credit", accountId)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }

    @CircuitBreaker(name = "accountService")
    public void refund(
        Long accountId,
        TransferService.AccountOperationRequest request) {

        accountClient.post()
            .uri("/api/accounts/{id}/refund", accountId)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}
