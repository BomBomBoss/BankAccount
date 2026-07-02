package com.bank.platform.account.utils.exceptions;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(final String message) {
        super(message);
    }
}
