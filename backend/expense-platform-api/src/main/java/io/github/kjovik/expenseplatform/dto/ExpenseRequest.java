package io.github.kjovik.expenseplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(@NotNull @Positive Long amountMinor,
                             @NotNull @Size(max = 3) String currency,
                             @NotNull  @Size(max = 100) String category,
                             @Size (max = 500) String description) { }
