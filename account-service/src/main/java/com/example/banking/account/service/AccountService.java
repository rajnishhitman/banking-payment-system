package com.example.banking.account.service;

import com.example.banking.account.dto.AccountDtos.AccountResponse;
import com.example.banking.account.dto.AccountDtos.CreateAccountRequest;
import com.example.banking.account.dto.AccountDtos.OperationRequest;
import com.example.banking.account.entity.Account;
import com.example.banking.account.entity.AccountOperation;
import com.example.banking.account.repository.AccountOperationRepository;
import com.example.banking.account.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final AccountOperationRepository operations;

    public AccountService(AccountRepository accounts, AccountOperationRepository operations) {
        this.accounts = accounts;
        this.operations = operations;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        return toResponse(accounts.save(new Account(request.ownerName(), request.initialBalance())));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accounts.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(Long id) {
        return toResponse(findAccount(id));
    }

    @Transactional
    public AccountResponse debit(Long id, OperationRequest request) {
        return changeBalance(id, request, "DEBIT");
    }

    @Transactional
    public AccountResponse credit(Long id, OperationRequest request) {
        return changeBalance(id, request, "CREDIT");
    }

    @Transactional
    public AccountResponse refund(Long id, OperationRequest request) {
        return changeBalance(id, request, "REFUND");
    }

    private AccountResponse changeBalance(Long id, OperationRequest request, String type) {
        var previousOperation = operations.findByOperationId(request.operationId());
        if (previousOperation.isPresent()) {
            AccountOperation previous = previousOperation.get();
            boolean sameOperation = previous.getAccountId().equals(id)
                    && previous.getType().equals(type)
                    && previous.getAmount().compareTo(request.amount()) == 0;
            if (!sameOperation) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "operationId already used for a different operation");
            }
            return findById(id);
        }

        Account account = accounts.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if ("DEBIT".equals(type)) {
            if (account.getBalance().compareTo(request.amount()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
            }
            account.setBalance(account.getBalance().subtract(request.amount()));
        } else {
            account.setBalance(account.getBalance().add(request.amount()));
        }

        accounts.save(account);
        operations.save(new AccountOperation(request.operationId(), id, type, request.amount()));
        return toResponse(account);
    }

    private Account findAccount(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerName(), account.getBalance());
    }
}
