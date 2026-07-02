package com.bank.platform.account.service;

import com.bank.platform.account.dto.BalancePointResponse;
import com.bank.platform.account.entity.Account;
import com.bank.platform.account.entity.Transaction;
import com.bank.platform.account.enums.TransactionType;
import com.bank.platform.account.repository.TransactionRepository;
import com.bank.platform.account.utils.exceptions.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;


    public Transaction saveTransaction(final Account account, final TransactionType transactionType, final BigDecimal amount, final BigDecimal balance) {
        final Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionTypeId(transactionType.getId());
        transaction.setAmount(amount);
        transaction.setCurrencyId(account.getCurrencyId());
        transaction.setBalanceAfter(balance);
        transaction.setDescription(resolveDescription(account, transactionType, null));

        return transactionRepository.save(transaction);
    }

    public Transaction saveTransaction(final Account account, final TransactionType transactionType, final BigDecimal amount, final BigDecimal balance, final BigDecimal rate) {
        final Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionTypeId(transactionType.getId());
        transaction.setAmount(amount);
        transaction.setCurrencyId(account.getCurrencyId());
        transaction.setBalanceAfter(balance);
        transaction.setDescription(resolveDescription(account, transactionType, rate));

        return transactionRepository.save(transaction);
    }

    public Page<Transaction> getPageableTransactionHistory(final long accountId, final Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    public Transaction getTransaction(final Long transactionId) {
        return transactionRepository.findById(transactionId).orElseThrow(() -> new TransactionNotFoundException("Transaction with id '%d' not found".formatted(transactionId)));
    }

    public List<Transaction> gerOrderedBalance(final Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
    }

    private String resolveDescription(final Account account, final TransactionType transactionType, final BigDecimal rate) {
        return switch (transactionType) {
            case DEPOSIT -> "Deposit to account: '%d'".formatted(account.getId());
            case WITHDRAWAL -> "Withdrawal from account: '%d'".formatted(account.getId());
            case EXCHANGE_OUT -> "Exchange from account '%d' with rate '%s'".formatted(account.getId(), rate);
            case EXCHANGE_IN -> "Exchange to account '%d' with rate '%s'".formatted(account.getId(), rate);
        };
    }
}
