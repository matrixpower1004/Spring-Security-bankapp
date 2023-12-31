package com.matrix.bank.domain.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * author         : Jason Lee
 * date           : 2023-07-23
 * description    :
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>, Dao {
}
