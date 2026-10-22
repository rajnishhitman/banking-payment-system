package com.example.banking.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record TransferRequest(
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String currency
    ) {}

    public record PaymentResponse(String id, Long fromAccountId, Long toAccountId,
            BigDecimal amount, String currency, String status, String failureReason) {}
}
