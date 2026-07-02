package com.bank.platform.account.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BalancePointResponse(LocalDateTime date, BigDecimal balance) {}
