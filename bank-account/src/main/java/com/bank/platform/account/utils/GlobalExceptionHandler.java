package com.bank.platform.account.utils;

import com.bank.platform.account.dto.ErrorResponse;
import com.bank.platform.account.utils.exceptions.AccountNotFoundException;
import com.bank.platform.account.utils.exceptions.CurrencyNotFoundException;
import com.bank.platform.account.utils.exceptions.ExchangeRateNotFoundException;
import com.bank.platform.account.utils.exceptions.InsufficientFundsException;
import com.bank.platform.account.utils.exceptions.TransactionNotFoundException;
import com.bank.platform.account.utils.exceptions.TransactionTypeNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            AccountNotFoundException.class,
            TransactionNotFoundException.class,
            TransactionTypeNotFoundException.class,
            CurrencyNotFoundException.class,
            ExchangeRateNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(final RuntimeException exception, final HttpServletRequest request) {
        return buildErrorResponse(exception, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(final InsufficientFundsException exception, final HttpServletRequest request) {
        return buildErrorResponse(exception, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            final ConstraintViolationException exception,
            final HttpServletRequest request) {

        String message = exception.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, message);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(final Exception exception, final HttpServletRequest request) {
        return buildErrorResponse(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(final Exception exception, final HttpServletRequest request, final HttpStatus httpStatus) {
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(final HttpServletRequest request, final HttpStatus httpStatus, final String message) {
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
