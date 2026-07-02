package com.bank.platform.account.controllers;

import com.bank.platform.account.dto.AccountBalanceResponse;
import com.bank.platform.account.dto.AmountRequest;
import com.bank.platform.account.dto.BalancePointResponse;
import com.bank.platform.account.dto.ExchangeRequest;
import com.bank.platform.account.dto.TransactionResponse;
import com.bank.platform.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;


    @GetMapping
    public ResponseEntity<List<AccountBalanceResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{accountId}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable final Long accountId, @PathVariable final Long transactionId) {
        return ResponseEntity.ok(accountService.getTransaction(accountId, transactionId));
    }

    @GetMapping("/{accountId}/balance-history")
    public ResponseEntity<List<BalancePointResponse>> getBalanceHistory(@PathVariable final Long accountId) {
        return ResponseEntity.ok(accountService.getBalanceHistory(accountId));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(@PathVariable final Long accountId, @RequestBody @Valid final AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountId, request.amount()));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountBalanceResponse> withdraw(@PathVariable final Long accountId, @RequestBody @Valid final AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountId, request.amount()));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(@PathVariable final Long accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @PostMapping("/{accountId}/exchange")
    public ResponseEntity<AccountBalanceResponse> exchange(@PathVariable final Long accountId, @RequestBody @Valid final ExchangeRequest request) {
        return ResponseEntity.ok(accountService.exchange(accountId, request.targetAccountId(), request.amount()));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@PathVariable final Long accountId,
                                                                     @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be >= 0") int page,
                                                                     @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be >= 1") @Max(value = 100, message = "Size must be <= 100") int size) {
        return ResponseEntity.ok(accountService.getTransactionHistory(accountId, page, size));
    }



}
