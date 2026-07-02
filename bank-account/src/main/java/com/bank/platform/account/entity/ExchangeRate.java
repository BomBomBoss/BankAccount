package com.bank.platform.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "exchange_rates")
@Data
public class ExchangeRate extends AuditableEntity {

    @Column
    private String fromCurrency;

    @Column
    private String toCurrency;

    @Column
    private BigDecimal rate;
}
