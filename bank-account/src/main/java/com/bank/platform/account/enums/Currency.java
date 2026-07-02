package com.bank.platform.account.enums;

import com.bank.platform.account.utils.exceptions.CurrencyNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Currency {

    EUR(1, "Euro"),
    USD(2, "US Dollar"),
    SEK(3, "Swedish Krona"),
    GBP(4, "British Pound"),
    VND(5, "Vietnamese Dong");

    private final int id;
    private final String name;

    public static String getCurrencyName(final int currencyId) {
        return Arrays.stream(values()).filter(currency -> currency.getId() == currencyId)
                .map(Currency::getName)
                .findFirst().orElseThrow(() -> new CurrencyNotFoundException("Currency with id: '%d' not found".formatted(currencyId)));
    }


}
