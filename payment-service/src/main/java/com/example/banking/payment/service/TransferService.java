package com.example.banking.payment.service;

import com.example.banking.payment.dto.PaymentDtos.TransferRequest;
import com.example.banking.payment.entity.Payment;
import com.example.banking.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {
    private final PaymentRepository payments;
    private final AccountServiceClient accountServiceClient;
    private final RestClient notificationClient;

    public TransferService(PaymentRepository payments, RestClient.Builder builder
        , AccountServiceClient accountServiceClient) {
        this.payments = payments;
        this.accountServiceClient = accountServiceClient;
        this.notificationClient = builder.baseUrl("http://localhost:8083").build();
    }

    public Payment transfer(TransferRequest request) {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = payments.save(new Payment(paymentId, request.fromAccountId(),
                request.toAccountId(), request.amount(), request.currency()));
        boolean debited = false;

        try {
            accountServiceClient.debit(request.fromAccountId()
                , new AccountOperationRequest(paymentId + "-DEBIT", request.amount()));
//            accountClient.post().uri("/api/accounts/{id}/debit", request.fromAccountId())
//                    .body(new AccountOperationRequest(paymentId + "-DEBIT", request.amount()))
//                    .retrieve().toBodilessEntity();
            debited = true;

            accountServiceClient.credit(request.toAccountId()
                , new AccountOperationRequest(paymentId + "-CREDIT", request.amount()));
//            accountClient.post().uri("/api/accounts/{id}/credit", request.toAccountId())
//                    .body(new AccountOperationRequest(paymentId + "-CREDIT", request.amount()))
//                    .retrieve().toBodilessEntity();
            payment.setStatus("COMPLETED");
            try {
                notificationClient.post().uri("/api/notifications")
                        .body(new NotificationRequest(paymentId, request.fromAccountId(),
                                request.toAccountId(), request.amount(), request.currency(), "TRANSFER_COMPLETED"))
                        .retrieve().toBodilessEntity();
            } catch (Exception ignored) {
                // Notification delivery is best-effort in this learning project.
            }
        } catch (Exception exception) {
            payment.setFailureReason(exception.getMessage() == null ? "Transfer failed" : exception.getMessage());
            if (debited) {
                try {
                    accountServiceClient.refund(request.fromAccountId()
                        , new AccountOperationRequest(paymentId + "-REFUND", request.amount()));
//                    accountClient.post().uri("/api/accounts/{id}/refund", request.fromAccountId())
//                            .body(new AccountOperationRequest(paymentId + "-REFUND", request.amount()))
//                            .retrieve().toBodilessEntity();
                    payment.setStatus("FAILED_REFUNDED");
                } catch (Exception refundException) {
                    payment.setStatus("PENDING_RECONCILIATION");
                    payment.setFailureReason("Transfer failed and automatic refund failed; manual reconciliation required");
                }
            } else {
                payment.setStatus("FAILED");
            }
        }
        return payments.save(payment);
    }

    public record AccountOperationRequest(String operationId, BigDecimal amount) {}
    public record NotificationRequest(String paymentId, Long fromAccountId, Long toAccountId,
            BigDecimal amount, String currency, String eventType) {}
}
