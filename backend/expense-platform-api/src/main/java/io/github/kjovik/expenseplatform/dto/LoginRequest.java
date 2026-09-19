package io.github.kjovik.expenseplatform.dto;

public record LoginRequest(String email, String password, String tenantName) { }
