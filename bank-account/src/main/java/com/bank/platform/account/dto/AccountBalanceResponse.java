package com.bank.platform.account.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountBalanceResponse(Long accountId, String currency, BigDecimal balance) {}
