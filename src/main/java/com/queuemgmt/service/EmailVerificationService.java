package com.queuemgmt.service;

import com.queuemgmt.exception.InvalidVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailService emailService;
    private final ConcurrentHashMap<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();

    /**
     * Generates a new pincode for the given email, stores it, and emails it.
     * Overwrites any previously pending code for that email (acts as "resend").
     */
    public void generateAndSendCode(String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        pendingCodes.put(email, new PendingCode(code, Instant.now().plus(CODE_TTL)));
        emailService.sendVerificationCode(email, code);
    }

    /**
     * Verifies a submitted pincode against the stored one for that email.
     * The code is consumed (removed) only once it is successfully verified,
     * so a mistyped code can be retried until it expires.
     */
    public void verifyCode(String email, String code) {
        PendingCode pending = pendingCodes.get(email);

        if (pending == null) {
            throw new InvalidVerificationException("No verification code was requested for this email");
        }
        if (Instant.now().isAfter(pending.expiresAt())) {
            pendingCodes.remove(email);
            throw new InvalidVerificationException("Verification code has expired");
        }
        if (!pending.code().equals(code)) {
            throw new InvalidVerificationException("Incorrect verification code");
        }

        pendingCodes.remove(email);
    }

    private record PendingCode(String code, Instant expiresAt) {
    }
}
