package com.bank.platform.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
@Data
public class Transaction extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column
    private Long transactionTypeId;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal balanceAfter;

    @Column
    private Integer currencyId;

    @Column
    private String description;



}
