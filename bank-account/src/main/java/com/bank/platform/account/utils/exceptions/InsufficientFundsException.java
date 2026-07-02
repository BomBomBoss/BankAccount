package com.bank.platform.account.utils.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(final String message) {
        super(message);
    }
}
