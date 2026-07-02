package com.bank.platform.account.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
}
