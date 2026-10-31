package com.example.banking.payment.service;

import com.example.banking.payment.dto.PaymentDtos.TransferRequest;
import com.example.banking.payment.dto.PaymentDtos.PaymentResponse;
import com.example.banking.payment.entity.Payment;
import com.example.banking.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final TransferService transferService;

    public PaymentService(PaymentRepository payments, TransferService transferService) {
        this.payments = payments;
        this.transferService = transferService;
    }

    public PaymentResponse transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must be different");
        }
        if (request.amount().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount can have at most 2 decimal places");
        }
        return toResponse(transferService.transfer(request));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return payments.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(String id) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getFromAccountId(), p.getToAccountId(),
                p.getAmount(), p.getCurrency(), p.getStatus(), p.getFailureReason());
    }
}
