package com.bank.platform.account.dto;

import com.bank.platform.account.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(Long id, TransactionType type, BigDecimal amount, String currency, BigDecimal balanceAfter, String description, LocalDateTime createdAt) {}
