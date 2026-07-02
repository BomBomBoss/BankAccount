package com.bank.platform.account.service;

import com.bank.platform.account.dto.AccountBalanceResponse;
import com.bank.platform.account.dto.BalancePointResponse;
import com.bank.platform.account.dto.TransactionResponse;
import com.bank.platform.account.entity.Account;
import com.bank.platform.account.entity.ExchangeRate;
import com.bank.platform.account.entity.Transaction;
import com.bank.platform.account.enums.TransactionType;
import com.bank.platform.account.repository.AccountRepository;
import com.bank.platform.account.repository.ExchangeRateRepository;
import com.bank.platform.account.utils.exceptions.AccountNotFoundException;
import com.bank.platform.account.utils.exceptions.ExchangeRateNotFoundException;
import com.bank.platform.account.utils.exceptions.InsufficientFundsException;
import com.bank.platform.account.utils.exceptions.TransactionNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final int EUR = 1;
    private static final int USD = 2;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ExchangeRateRepository exchangeRateRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ExternalLoggingService externalLoggingService;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = account(1L, new BigDecimal("100.00"), EUR);
    }


    @Nested
    class GetAllAccounts {

        @Test
        void returnsBalanceResponseForEveryAccount() {
            Account second = account(2L, new BigDecimal("250.00"), USD);
            when(accountRepository.findAll()).thenReturn(List.of(account, second));

            List<AccountBalanceResponse> result = accountService.getAllAccounts();

            assertThat(result).containsExactly(
                    AccountBalanceResponse.builder().accountId(1L).balance(new BigDecimal("100.00")).currency("Euro").build(),
                    AccountBalanceResponse.builder().accountId(2L).balance(new BigDecimal("250.00")).currency("US Dollar").build()
            );
        }

        @Test
        void returnsEmptyListWhenNoAccounts() {
            when(accountRepository.findAll()).thenReturn(List.of());

            assertThat(accountService.getAllAccounts()).isEmpty();
        }
    }

    @Nested
    class GetBalance {

        @Test
        void returnsBalanceForExistingAccount() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            AccountBalanceResponse result = accountService.getBalance(1L);

            assertThat(result.accountId()).isEqualTo(1L);
            assertThat(result.balance()).isEqualByComparingTo("100.00");
            assertThat(result.currency()).isEqualTo("Euro");
        }

        @Test
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getBalance(99L))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    class Deposit {

        @Test
        void increasesBalancePersistsAndRecordsTransaction() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            AccountBalanceResponse result = accountService.deposit(1L, new BigDecimal("50.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("150.00");
            assertThat(result.balance()).isEqualByComparingTo("150.00");
            verify(accountRepository).save(account);
            verify(transactionService).saveTransaction(
                    account, TransactionType.DEPOSIT, new BigDecimal("50.00"), new BigDecimal("150.00"));
        }

        @Test
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deposit(1L, BigDecimal.TEN))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(accountRepository, never()).save(any());
            verifyNoInteractions(transactionService);
        }
    }

    @Nested
    class Withdraw {

        @Test
        void decreasesBalanceLogsAndRecordsTransaction() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            AccountBalanceResponse result = accountService.withdraw(1L, new BigDecimal("40.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("60.00");
            assertThat(result.balance()).isEqualByComparingTo("60.00");
            verify(externalLoggingService).logWithdrawal(1L, new BigDecimal("40.00"), "Euro");
            verify(accountRepository).save(account);
            verify(transactionService).saveTransaction(
                    account, TransactionType.WITHDRAWAL, new BigDecimal("40.00"), new BigDecimal("60.00"));
        }

        @Test
        void throwsWhenFundsInsufficientAndDoesNotMutateState() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("500.00")))
                    .isInstanceOf(InsufficientFundsException.class);

            assertThat(account.getBalance()).isEqualByComparingTo("100.00");
            verify(accountRepository, never()).save(any());
            verifyNoInteractions(externalLoggingService, transactionService);
        }

        @Test
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.withdraw(1L, BigDecimal.TEN))
                    .isInstanceOf(AccountNotFoundException.class);
            verifyNoInteractions(externalLoggingService, transactionService);
        }
    }

    @Nested
    class Exchange {

        @Test

        void movesConvertedAmountBetweenAccounts() {
            Account target = account(2L, new BigDecimal("200.00"), USD);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("Euro", "US Dollar"))
                    .thenReturn(Optional.of(exchangeRate(new BigDecimal("1.1"))));

            AccountBalanceResponse result = accountService.exchange(1L, 2L, new BigDecimal("10.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("90.00");
            assertThat(target.getBalance()).isEqualByComparingTo("211.0000");
            assertThat(result.accountId()).isEqualTo(1L);
            assertThat(result.balance()).isEqualByComparingTo("90.00");

            verify(accountRepository).save(account);
            verify(accountRepository).save(target);
            verify(transactionService).saveTransaction(
                    account, TransactionType.EXCHANGE_OUT, new BigDecimal("10.00"),
                    new BigDecimal("90.00"), new BigDecimal("1.1"));
            verify(transactionService).saveTransaction(
                    target, TransactionType.EXCHANGE_IN, new BigDecimal("10.00"),
                    new BigDecimal("211.0000"), new BigDecimal("1.1"));
        }

        @Test
        void throwsWhenSourceAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.exchange(1L, 2L, BigDecimal.TEN))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Source");
            verifyNoInteractions(exchangeRateRepository, transactionService);
        }

        @Test
        void throwsWhenTargetAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.exchange(1L, 2L, BigDecimal.TEN))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Target");
            verifyNoInteractions(exchangeRateRepository, transactionService);
        }

        @Test
        void throwsWhenFundsInsufficient() {
            Account target = account(2L, new BigDecimal("200.00"), USD);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(target));

            assertThatThrownBy(() -> accountService.exchange(1L, 2L, new BigDecimal("500.00")))
                    .isInstanceOf(InsufficientFundsException.class);
            verifyNoInteractions(exchangeRateRepository, transactionService);
        }

        @Test
        void throwsWhenExchangeRateMissing() {
            Account target = account(2L, new BigDecimal("200.00"), USD);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("Euro", "US Dollar"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.exchange(1L, 2L, new BigDecimal("10.00")))
                    .isInstanceOf(ExchangeRateNotFoundException.class);
            verifyNoInteractions(transactionService);
        }
    }

    @Nested
    class GetTransaction {

        @Test
        void returnsTransactionThatBelongsToAccount() {
            Transaction tx = transaction(7L, account, TransactionType.DEPOSIT.getId(),
                    new BigDecimal("30.00"), EUR, new BigDecimal("130.00"), "Deposit");
            when(transactionService.getTransaction(7L)).thenReturn(tx);

            TransactionResponse result = accountService.getTransaction(1L, 7L);

            assertThat(result.id()).isEqualTo(7L);
            assertThat(result.type()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(result.amount()).isEqualByComparingTo("30.00");
            assertThat(result.currency()).isEqualTo("Euro");
            assertThat(result.balanceAfter()).isEqualByComparingTo("130.00");
        }

        @Test
        void throwsWhenTransactionBelongsToDifferentAccount() {
            Transaction tx = transaction(7L, account, TransactionType.DEPOSIT.getId(),
                    new BigDecimal("30.00"), EUR, new BigDecimal("130.00"), "Deposit");
            when(transactionService.getTransaction(7L)).thenReturn(tx);

            assertThatThrownBy(() -> accountService.getTransaction(999L, 7L))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    class GetBalanceHistory {

        @Test
        void mapsOrderedTransactionsToBalancePoints() {
            LocalDateTime t1 = LocalDateTime.of(2026, 1, 1, 10, 0);
            LocalDateTime t2 = LocalDateTime.of(2026, 1, 2, 10, 0);
            Transaction first = transaction(1L, account, TransactionType.DEPOSIT.getId(),
                    new BigDecimal("50.00"), EUR, new BigDecimal("50.00"), "d", t1);
            Transaction second = transaction(2L, account, TransactionType.DEPOSIT.getId(),
                    new BigDecimal("50.00"), EUR, new BigDecimal("100.00"), "d", t2);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(transactionService.gerOrderedBalance(1L)).thenReturn(List.of(first, second));

            List<BalancePointResponse> result = accountService.getBalanceHistory(1L);

            assertThat(result).containsExactly(
                    BalancePointResponse.builder().date(t1).balance(new BigDecimal("50.00")).build(),
                    BalancePointResponse.builder().date(t2).balance(new BigDecimal("100.00")).build()
            );
        }

        @Test
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getBalanceHistory(1L))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(transactionService, never()).gerOrderedBalance(anyLong());
        }
    }


    @Nested
    class GetTransactionHistory {

        @Test
        void returnsPageOfMappedTransactions() {
            Transaction tx = transaction(5L, account, TransactionType.WITHDRAWAL.getId(),
                    new BigDecimal("20.00"), EUR, new BigDecimal("80.00"), "w");
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(transactionService.getPageableTransactionHistory(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(tx), PageRequest.of(0, 10), 1));

            Page<TransactionResponse> result = accountService.getTransactionHistory(1L, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).singleElement().satisfies(r -> {
                assertThat(r.id()).isEqualTo(5L);
                assertThat(r.type()).isEqualTo(TransactionType.WITHDRAWAL);
                assertThat(r.balanceAfter()).isEqualByComparingTo("80.00");
            });
        }

        @Test
        void requestsCorrectPageableSortedByCreatedAtDescending() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(transactionService.getPageableTransactionHistory(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            accountService.getTransactionHistory(1L, 2, 25);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionService).getPageableTransactionHistory(eq(1L), captor.capture());
            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(2);
            assertThat(pageable.getPageSize()).isEqualTo(25);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }

        @Test
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getTransactionHistory(1L, 0, 10))
                    .isInstanceOf(AccountNotFoundException.class);
            verifyNoInteractions(transactionService);
        }
    }


    private static Account account(final Long id, final BigDecimal balance, final int currencyId) {
        Account account = new Account();
        ReflectionTestUtils.setField(account, "id", id);
        account.setBalance(balance);
        account.setCurrencyId(currencyId);
        return account;
    }

    private static Transaction transaction(final Long id, final Account account, final long typeId,
                                           final BigDecimal amount, final int currencyId,
                                           final BigDecimal balanceAfter, final String description) {
        return transaction(id, account, typeId, amount, currencyId, balanceAfter, description, null);
    }

    private static Transaction transaction(final Long id, final Account account, final long typeId,
                                           final BigDecimal amount, final int currencyId,
                                           final BigDecimal balanceAfter, final String description,
                                           final LocalDateTime createdAt) {
        Transaction transaction = new Transaction();
        ReflectionTestUtils.setField(transaction, "id", id);
        transaction.setAccount(account);
        transaction.setTransactionTypeId(typeId);
        transaction.setAmount(amount);
        transaction.setCurrencyId(currencyId);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        ReflectionTestUtils.setField(transaction, "createdAt", createdAt);
        return transaction;
    }

    private static ExchangeRate exchangeRate(final BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate(rate);
        return exchangeRate;
    }
}
