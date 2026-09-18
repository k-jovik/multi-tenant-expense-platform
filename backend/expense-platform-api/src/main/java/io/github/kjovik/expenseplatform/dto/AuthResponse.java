package io.github.kjovik.expenseplatform.dto;


import java.util.UUID;

public record AuthResponse(String token, UUID id, String role,UUID tenantId) { }
