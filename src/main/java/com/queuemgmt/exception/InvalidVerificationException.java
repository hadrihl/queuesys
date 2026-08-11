package com.queuemgmt.exception;

/**
 * Thrown when an email pincode fails verification (missing, expired, or incorrect).
 */
public class InvalidVerificationException extends RuntimeException {

    public InvalidVerificationException(String message) {
        super(message);
    }

    public InvalidVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
