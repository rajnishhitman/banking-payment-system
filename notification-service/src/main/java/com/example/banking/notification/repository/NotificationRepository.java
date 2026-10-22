package com.example.banking.notification.repository;

import com.example.banking.notification.entity.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByPaymentId(String paymentId);
}
