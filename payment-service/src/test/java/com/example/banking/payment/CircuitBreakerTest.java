package com.example.banking.payment;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CircuitBreakerTest {

    @Test
    void shouldOpenCircuitAfterFailureThresholdIsReached() {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowSize(4)
            .minimumNumberOfCalls(4)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(15))
            .build();

        CircuitBreaker circuitBreaker =
            CircuitBreaker.of("testAccountService", config);

        for (int i = 0; i < 4; i++) {
            try {
                circuitBreaker.executeRunnable(() -> {
                    throw new RuntimeException("Account service unavailable");
                });
            } catch (RuntimeException ignored) {
                // Expected simulated downstream failure.
            }
        }

        assertEquals(
            CircuitBreaker.State.OPEN,
            circuitBreaker.getState()
        );

        assertThrows(
            io.github.resilience4j.circuitbreaker.CallNotPermittedException.class,
            () -> circuitBreaker.executeRunnable(() -> {
                // This should not execute when the breaker is OPEN.
            })
        );
    }
}
