package com.example.banking.payment.controller;

import com.example.banking.payment.dto.PaymentDtos.PaymentResponse;
import com.example.banking.payment.dto.PaymentDtos.TransferRequest;
import com.example.banking.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse transfer(@Valid @RequestBody TransferRequest request) {
        return paymentService.transfer(request);
    }

    @GetMapping
    public List<PaymentResponse> findAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(@PathVariable("id") String id) {
        return paymentService.findById(id);
    }
}
