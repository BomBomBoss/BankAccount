package com.bank.platform.account.utils.exceptions;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(final String message) {
        super(message);
    }
}
