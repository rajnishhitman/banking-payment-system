package com.example.banking.notification.controller;

import com.example.banking.notification.dto.NotificationDtos.CreateNotificationRequest;
import com.example.banking.notification.dto.NotificationDtos.NotificationResponse;
import com.example.banking.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.create(request);
    }

    @GetMapping
    public List<NotificationResponse> findAll() {
        return notificationService.findAll();
    }
}
