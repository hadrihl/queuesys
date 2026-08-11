package com.queuemgmt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Send the registration verification pincode.
     * Unlike the other notifications below, a delivery failure here is thrown
     * rather than swallowed, since the user has no other way to get the code.
     */
    public void sendVerificationCode(String email, String pincode) {
        String message = String.format(
            "Your verification code is: %s%n%nThis code expires in 10 minutes.",
            pincode
        );

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(email);
        mailMessage.setSubject("Your Verification Code");
        mailMessage.setText(message);

        mailSender.send(mailMessage);
        log.info("Verification code sent to {}", email);
    }

    /**
     * Send confirmation with tracking link after successful registration
     */
    public void sendRegistrationEmail(String email, String userName, String trackingNumber, String accessToken) {
        String trackingLink = baseUrl + "/queue/track/" + accessToken;
        String message = String.format(
            "Hello %s! You are registered in the queue. Tracking Number: %s. Track your status: %s",
            userName, trackingNumber, trackingLink
        );

        send(email, "You're in the Queue", message);
    }

    /**
     * Send notification when it's user's turn
     */
    public void sendYourTurnNotification(String email, String userName, String accessToken) {
        String trackingLink = baseUrl + "/queue/track/" + accessToken;
        String message = String.format(
            "Hello %s! It's your turn now. Please proceed to the service counter. Track: %s",
            userName, trackingLink
        );

        send(email, "It's Your Turn", message);
    }

    /**
     * Send notification when service is completed
     */
    public void sendServiceCompletedNotification(String email, String userName) {
        String message = String.format(
            "Hello %s! Your service has been completed. Thank you for using our queue system!",
            userName
        );

        send(email, "Service Completed", message);
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
