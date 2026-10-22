package com.example.banking.notification.service;

import com.example.banking.notification.dto.NotificationDtos.CreateNotificationRequest;
import com.example.banking.notification.dto.NotificationDtos.NotificationResponse;
import com.example.banking.notification.entity.Notification;
import com.example.banking.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = repository.findByPaymentId(request.paymentId())
                .orElseGet(() -> repository.save(new Notification(request.paymentId(),
                        request.fromAccountId(), request.toAccountId(), request.amount(),
                        request.currency(), request.eventType())));
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getPaymentId(), n.getFromAccountId(),
                n.getToAccountId(), n.getAmount(), n.getCurrency(), n.getEventType());
    }
}
