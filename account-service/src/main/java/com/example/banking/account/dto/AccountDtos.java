package com.example.banking.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class AccountDtos {
    private AccountDtos() {}

    public record CreateAccountRequest(
            @NotBlank String ownerName,
            @NotNull @DecimalMin("0.00") BigDecimal initialBalance
    ) {}

    public record AccountResponse(Long id, String ownerName, BigDecimal balance) {}

    public record OperationRequest(
            @NotBlank String operationId,
            @NotNull @DecimalMin("0.01") BigDecimal amount
    ) {}
}
