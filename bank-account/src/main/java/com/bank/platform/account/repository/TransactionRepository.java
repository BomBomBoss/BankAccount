package com.bank.platform.account.repository;

import com.bank.platform.account.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountId(final long accountId, final Pageable pageable);

    List<Transaction> findByAccountIdOrderByCreatedAtAsc(final long accountId);

}
