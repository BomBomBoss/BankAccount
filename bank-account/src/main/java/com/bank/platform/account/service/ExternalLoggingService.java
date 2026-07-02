package com.bank.platform.account.service;

import com.bank.platform.account.utils.exceptions.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalLoggingService {

    private final RestClient restClient;


    public void logWithdrawal(final Long accountId, final BigDecimal amount, final String currency) {
        try {
            restClient.get()
                    .uri("https://httpstat.us/200")
                    .retrieve()
                    .toBodilessEntity();
            log.info("External logging succeeded for withdrawal: account={}, amount={} {}",
                    accountId, amount, currency);
        } catch (Exception e) {
            log.error("External logging call failed for account={}", accountId, e);
            throw new ExternalServiceException("External logging service unavailable");
        }
    }


}
