package io.github.kjovik.expenseplatform.repository;

import java.util.UUID;

public interface AccountBalanceProjection {
    UUID getAccountId();
    Long getBalance();
}
