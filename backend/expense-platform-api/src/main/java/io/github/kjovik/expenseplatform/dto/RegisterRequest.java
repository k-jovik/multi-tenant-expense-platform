package io.github.kjovik.expenseplatform.dto;


public record RegisterRequest(String email, String password, String tenantName, String fullName) { }
