package com.fts.tenantbasededuportal.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(final String message) {
        super(message);
    }
}