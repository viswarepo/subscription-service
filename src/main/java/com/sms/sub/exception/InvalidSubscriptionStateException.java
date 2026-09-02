package com.sms.sub.exception;

public class InvalidSubscriptionStateException extends RuntimeException {
    public InvalidSubscriptionStateException(String message) {
        super(message);
    }
}
