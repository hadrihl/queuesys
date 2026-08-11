package com.queuemgmt.service;

import com.queuemgmt.dto.QueueStatusResponse;
import com.queuemgmt.dto.RegistrationRequest;
import com.queuemgmt.model.QueueEntry;
import com.queuemgmt.model.QueueStatus;
import com.queuemgmt.repository.QueueEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final QueueEntryRepository queueEntryRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    /**
     * Register a new user in the queue
     */
    @Transactional
    public QueueStatusResponse registerUser(RegistrationRequest request) {
        // Verify the emailed pincode before creating the queue entry
        emailVerificationService.verifyCode(request.getEmail(), request.getPincode());

        // Generate tracking number and access token
        String trackingNumber = generateTrackingNumber();
        String accessToken = UUID.randomUUID().toString();

        // Get next queue number
        Integer maxQueueNumber = queueEntryRepository.findMaxQueueNumber();
        Integer newQueueNumber = (maxQueueNumber == null ? 0 : maxQueueNumber) + 1;

        // Create queue entry
        QueueEntry entry = new QueueEntry();
        entry.setTrackingNumber(trackingNumber);
        entry.setEmail(request.getEmail());
        entry.setUserName(request.getUserName());
        entry.setQueueNumber(newQueueNumber);
        entry.setStatus(QueueStatus.WAITING);
        entry.setAccessToken(accessToken);
        entry.setLinkActive(true);
        entry.setRegisteredAt(LocalDateTime.now());

        entry = queueEntryRepository.save(entry);

        // Send confirmation email with tracking link
        emailService.sendRegistrationEmail(
            request.getEmail(),
            request.getUserName(),
            trackingNumber,
            accessToken
        );

        log.info("Registered user {} with tracking number {} and queue number {}",
            request.getUserName(), trackingNumber, newQueueNumber);

        return buildStatusResponse(entry);
    }

    /**
     * Get queue status by tracking number
     */
    public QueueStatusResponse getStatusByTrackingNumber(String trackingNumber) {
        QueueEntry entry = queueEntryRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Tracking number not found"));
        
        return buildStatusResponse(entry);
    }

    /**
     * Get queue status by access token (for email tracking link)
     */
    public QueueStatusResponse getStatusByAccessToken(String accessToken) {
        QueueEntry entry = queueEntryRepository.findByAccessToken(accessToken)
            .orElseThrow(() -> new RuntimeException("Invalid or expired link"));
        
        if (!entry.getLinkActive()) {
            throw new RuntimeException("This link is no longer active");
        }
        
        return buildStatusResponse(entry);
    }

    /**
     * Call the next person in queue
     */
    @Transactional
    public QueueStatusResponse callNextInQueue() {
        List<QueueEntry> waitingEntries = queueEntryRepository.findByStatusOrderByQueueNumberAsc(QueueStatus.WAITING);
        
        if (waitingEntries.isEmpty()) {
            throw new RuntimeException("No one in queue");
        }

        QueueEntry entry = waitingEntries.get(0);
        entry.setStatus(QueueStatus.CALLED);
        entry.setCalledAt(LocalDateTime.now());
        entry = queueEntryRepository.save(entry);

        // Send notification
        emailService.sendYourTurnNotification(
            entry.getEmail(),
            entry.getUserName(),
            entry.getAccessToken()
        );

        log.info("Called queue number {} - {}", entry.getQueueNumber(), entry.getUserName());

        return buildStatusResponse(entry);
    }

    /**
     * Mark entry as in service
     */
    @Transactional
    public QueueStatusResponse startService(String trackingNumber) {
        QueueEntry entry = queueEntryRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Tracking number not found"));
        
        entry.setStatus(QueueStatus.IN_SERVICE);
        entry = queueEntryRepository.save(entry);

        log.info("Started service for queue number {} - {}", entry.getQueueNumber(), entry.getUserName());

        return buildStatusResponse(entry);
    }

    /**
     * Complete service for a user
     */
    @Transactional
    public QueueStatusResponse completeService(String trackingNumber) {
        QueueEntry entry = queueEntryRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Tracking number not found"));
        
        entry.setStatus(QueueStatus.COMPLETED);
        entry.setCompletedAt(LocalDateTime.now());
        entry.setLinkActive(false);  // Deactivate the link
        entry = queueEntryRepository.save(entry);

        // Send completion notification
        emailService.sendServiceCompletedNotification(
            entry.getEmail(),
            entry.getUserName()
        );

        log.info("Completed service for queue number {} - {}", entry.getQueueNumber(), entry.getUserName());

        return buildStatusResponse(entry);
    }

    /**
     * Get all active queue entries
     */
    public List<QueueEntry> getActiveQueue() {
        return queueEntryRepository.findByStatusInOrderByQueueNumberAsc(
            List.of(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.IN_SERVICE)
        );
    }

    /**
     * Generate a unique tracking number
     */
    private String generateTrackingNumber() {
        return "QMS-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * Build status response with calculated fields
     */
    private QueueStatusResponse buildStatusResponse(QueueEntry entry) {
        Integer peopleAhead = queueEntryRepository.countQueueAhead(entry.getQueueNumber());
        
        String message = switch (entry.getStatus()) {
            case WAITING -> "You are in the queue. " + peopleAhead + " people ahead of you.";
            case CALLED -> "It's your turn! Please proceed to the service counter.";
            case IN_SERVICE -> "You are currently being served.";
            case COMPLETED -> "Your service has been completed. Thank you!";
            case CANCELLED -> "Your queue entry has been cancelled.";
        };

        QueueStatusResponse response = new QueueStatusResponse();
        response.setTrackingNumber(entry.getTrackingNumber());
        response.setUserName(entry.getUserName());
        response.setQueueNumber(entry.getQueueNumber());
        response.setPeopleAhead(peopleAhead);
        response.setStatus(entry.getStatus());
        response.setRegisteredAt(entry.getRegisteredAt());
        response.setCalledAt(entry.getCalledAt());
        response.setCompletedAt(entry.getCompletedAt());
        response.setMessage(message);

        return response;
    }
}
