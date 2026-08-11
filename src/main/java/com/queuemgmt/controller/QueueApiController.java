package com.queuemgmt.controller;

import com.queuemgmt.dto.EmailRequest;
import com.queuemgmt.dto.QueueStatusResponse;
import com.queuemgmt.dto.RegistrationRequest;
import com.queuemgmt.exception.InvalidVerificationException;
import com.queuemgmt.model.QueueEntry;
import com.queuemgmt.service.EmailVerificationService;
import com.queuemgmt.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QueueApiController {

    private final QueueService queueService;
    private final EmailVerificationService emailVerificationService;

    /**
     * Send a verification pincode to the given email address
     */
    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody EmailRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            emailVerificationService.generateAndSendCode(request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Register a new user in the queue
     */
    @PostMapping("/register")
    public ResponseEntity<QueueStatusResponse> register(@RequestBody RegistrationRequest request) {
        try {
            QueueStatusResponse response = queueService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (InvalidVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get status by tracking number
     */
    @GetMapping("/status/{trackingNumber}")
    public ResponseEntity<QueueStatusResponse> getStatus(@PathVariable String trackingNumber) {
        try {
            QueueStatusResponse response = queueService.getStatusByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get status by access token (for email tracking links)
     */
    @GetMapping("/status/token/{accessToken}")
    public ResponseEntity<QueueStatusResponse> getStatusByToken(@PathVariable String accessToken) {
        try {
            QueueStatusResponse response = queueService.getStatusByAccessToken(accessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Admin: Call next person in queue
     */
    @PostMapping("/call-next")
    public ResponseEntity<QueueStatusResponse> callNext() {
        try {
            QueueStatusResponse response = queueService.callNextInQueue();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Admin: Start service for a user
     */
    @PostMapping("/start-service/{trackingNumber}")
    public ResponseEntity<QueueStatusResponse> startService(@PathVariable String trackingNumber) {
        try {
            QueueStatusResponse response = queueService.startService(trackingNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Admin: Complete service for a user
     */
    @PostMapping("/complete/{trackingNumber}")
    public ResponseEntity<QueueStatusResponse> completeService(@PathVariable String trackingNumber) {
        try {
            QueueStatusResponse response = queueService.completeService(trackingNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Admin: Get all active queue entries
     */
    @GetMapping("/active")
    public ResponseEntity<List<QueueEntry>> getActiveQueue() {
        try {
            List<QueueEntry> entries = queueService.getActiveQueue();
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
