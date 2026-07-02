package com.bank.platform.account.enums;

import com.bank.platform.account.utils.exceptions.TransactionTypeNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum TransactionType {
    DEPOSIT(1),
    WITHDRAWAL(2),
    EXCHANGE_IN(3),
    EXCHANGE_OUT(4);

    private final long id;

    public static TransactionType getType(long transactionTypeId) {
        return Arrays.stream(values()).filter(type -> type.getId() == transactionTypeId).findFirst().orElseThrow(() -> new TransactionTypeNotFoundException("Transaction type id '%s' not supported".formatted(transactionTypeId)));
    }
}
