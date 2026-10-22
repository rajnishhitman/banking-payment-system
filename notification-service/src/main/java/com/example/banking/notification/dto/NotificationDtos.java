package com.example.banking.notification.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record CreateNotificationRequest(
            @NotBlank String paymentId,
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String eventType
    ) {}

    public record NotificationResponse(Long id, String paymentId, Long fromAccountId,
            Long toAccountId, BigDecimal amount, String currency, String eventType) {}
}
