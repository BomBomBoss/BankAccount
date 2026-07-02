package com.bank.platform.account.utils.exceptions;

public class TransactionTypeNotFoundException extends RuntimeException {
    public TransactionTypeNotFoundException(final String message) {
        super(message);
    }
}
