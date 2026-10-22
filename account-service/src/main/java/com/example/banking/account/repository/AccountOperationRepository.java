package com.example.banking.account.repository;

import com.example.banking.account.entity.AccountOperation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    Optional<AccountOperation> findByOperationId(String operationId);
}
