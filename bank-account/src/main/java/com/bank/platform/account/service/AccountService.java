package com.bank.platform.account.service;

import com.bank.platform.account.dto.AccountBalanceResponse;
import com.bank.platform.account.dto.BalancePointResponse;
import com.bank.platform.account.dto.TransactionResponse;
import com.bank.platform.account.entity.Account;
import com.bank.platform.account.entity.ExchangeRate;
import com.bank.platform.account.entity.Transaction;
import com.bank.platform.account.enums.Currency;
import com.bank.platform.account.enums.TransactionType;
import com.bank.platform.account.repository.AccountRepository;
import com.bank.platform.account.repository.ExchangeRateRepository;
import com.bank.platform.account.utils.exceptions.AccountNotFoundException;
import com.bank.platform.account.utils.exceptions.ExchangeRateNotFoundException;
import com.bank.platform.account.utils.exceptions.InsufficientFundsException;
import com.bank.platform.account.utils.exceptions.TransactionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final TransactionService transactionService;
    private final ExternalLoggingService externalLoggingService;


    public List<AccountBalanceResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::getAccountBalanceResponse)
                .toList();
    }

    public TransactionResponse getTransaction(final Long accountId, final Long transactionId) {
        final Transaction transaction = transactionService.getTransaction(transactionId);

        if (!isTransactionBelongsToAccount(transaction, accountId)) {
            throw new TransactionNotFoundException("Transaction '%d' do not belongs to account '%d'".formatted(transaction.getId(), accountId));
        }

        return getTransactionResponse(transaction);
    }

    public List<BalancePointResponse> getBalanceHistory(final Long accountId) {
        accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account id: '%d' not found".formatted(accountId)));

        return transactionService.gerOrderedBalance(accountId).stream()
                .map(transaction -> BalancePointResponse.builder().date(transaction.getCreatedAt()).balance(transaction.getBalanceAfter()).build())
                .toList();

    }

    @Transactional
    public AccountBalanceResponse deposit(final Long accountId, final BigDecimal amount) {
        final Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account id: '%d' not found".formatted(accountId)));

        final BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        transactionService.saveTransaction(account, TransactionType.DEPOSIT, amount, newBalance);

        return getAccountBalanceResponse(account);
    }

    @Transactional
    public AccountBalanceResponse withdraw(final Long accountId, final BigDecimal amount) {
        final Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account id: '%d' not found".formatted(accountId)));

        if (isNotEnoughFunds(account, amount)) {
            throw new InsufficientFundsException("Account id '%d' has insufficient funds".formatted(accountId));
        }

        externalLoggingService.logWithdrawal(accountId, amount, Currency.getCurrencyName(account.getCurrencyId()));

        final BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        transactionService.saveTransaction(account, TransactionType.WITHDRAWAL, amount, newBalance);

        return getAccountBalanceResponse(account);
    }

    @Transactional
    public AccountBalanceResponse exchange(final Long sourceAccountId, final Long targetAccountId, final BigDecimal amount) {
        final Account accountFrom = accountRepository.findById(sourceAccountId).orElseThrow(() -> new AccountNotFoundException("Source account id: '%d' not found".formatted(sourceAccountId)));
        final Account accountTo = accountRepository.findById(targetAccountId).orElseThrow(() -> new AccountNotFoundException("Target account id: '%d' not found".formatted(targetAccountId)));

        if (isNotEnoughFunds(accountFrom, amount)) {
            throw new InsufficientFundsException("Account id '%d' has insufficient funds".formatted(sourceAccountId));
        }

        final String currencyFrom = Currency.getCurrencyName(accountFrom.getCurrencyId());
        final String currencyTo = Currency.getCurrencyName(accountTo.getCurrencyId());

        final BigDecimal rate = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(currencyFrom, currencyTo)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Exchange rate not found for pair from '%s' to '%s".formatted(currencyFrom, currencyTo)));

        final BigDecimal convertedAmount = amount.multiply(rate).setScale(4, java.math.RoundingMode.HALF_UP);

        final BigDecimal sourceNewBalance = accountFrom.getBalance().subtract(amount);
        accountFrom.setBalance(sourceNewBalance);
        accountRepository.save(accountFrom);

        transactionService.saveTransaction(accountFrom, TransactionType.EXCHANGE_OUT, amount, sourceNewBalance, rate);


        final BigDecimal targetNewBalance = accountTo.getBalance().add(convertedAmount);
        accountTo.setBalance(targetNewBalance);
        accountRepository.save(accountTo);

        transactionService.saveTransaction(accountTo, TransactionType.EXCHANGE_IN, amount, targetNewBalance, rate);

        return getAccountBalanceResponse(accountFrom);
    }

    public Page<TransactionResponse> getTransactionHistory(final Long accountId, final int page, final int size) {
        accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account id: '%d' not found".formatted(accountId)));

        final Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return transactionService.getPageableTransactionHistory(accountId, pageable).map(this::getTransactionResponse);
    }


    public AccountBalanceResponse getBalance(final Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account id: '%d' not found".formatted(accountId)));
        return getAccountBalanceResponse(account);
    }


    private AccountBalanceResponse getAccountBalanceResponse(final Account account) {
        return AccountBalanceResponse.builder()
                .accountId(account.getId())
                .balance(account.getBalance())
                .currency(Currency.getCurrencyName(account.getCurrencyId()))
                .build();
    }

    private TransactionResponse getTransactionResponse(final Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(TransactionType.getType(transaction.getTransactionTypeId()))
                .amount(transaction.getAmount())
                .currency(Currency.getCurrencyName(transaction.getCurrencyId()))
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private boolean isNotEnoughFunds(final Account account, final BigDecimal amountToWithdraw) {
        return account.getBalance().compareTo(amountToWithdraw) < 0;
    }

    private boolean isTransactionBelongsToAccount(final Transaction transaction, final Long accountId) {
        return transaction.getAccount().getId().equals(accountId);
    }

}
