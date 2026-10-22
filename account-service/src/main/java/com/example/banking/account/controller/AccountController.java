package com.example.banking.account.controller;

import com.example.banking.account.dto.AccountDtos.AccountResponse;
import com.example.banking.account.dto.AccountDtos.CreateAccountRequest;
import com.example.banking.account.dto.AccountDtos.OperationRequest;
import com.example.banking.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable("id") Long id) {
        return accountService.findById(id);
    }

    @PostMapping("/{id}/debit")
    public AccountResponse debit(@PathVariable("id") Long id, @Valid @RequestBody OperationRequest request) {
        return accountService.debit(id, request);
    }

    @PostMapping("/{id}/credit")
    public AccountResponse credit(@PathVariable("id") Long id, @Valid @RequestBody OperationRequest request) {
        return accountService.credit(id, request);
    }

    @PostMapping("/{id}/refund")
    public AccountResponse refund(@PathVariable("id") Long id, @Valid @RequestBody OperationRequest request) {
        return accountService.refund(id, request);
    }
}
