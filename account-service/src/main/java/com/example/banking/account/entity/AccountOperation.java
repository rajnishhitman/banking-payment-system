package com.example.banking.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "account_operations",
        uniqueConstraints = @UniqueConstraint(columnNames = "operationId")
)
public class AccountOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String operationId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AccountOperation() {
    }

    public AccountOperation(String operationId, Long accountId, String type, BigDecimal amount) {
        this.operationId = operationId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getOperationId() {
        return operationId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
