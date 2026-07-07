package com.fts.tenantbasededuportal.exception;

public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(final String message) {
        super(message);
    }

    public EmailDeliveryException(final String message,
            final Throwable cause) {

        super(message, cause);
    }
}
