package com.bank.platform.account.utils.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
