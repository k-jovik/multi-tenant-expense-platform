package io.github.kjovik.expenseplatform.dto;

import io.github.kjovik.expenseplatform.entity.Account;

import java.util.UUID;

public record AccountResponse(UUID id, String name, String type, Long balanceMinor) {
    public static AccountResponse from (Account account,Long balanceMinor) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType().name(),
                balanceMinor
        );
    }
}
