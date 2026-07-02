package com.bank.platform.account.utils.exceptions;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(final String message) {
        super(message);
    }
}
