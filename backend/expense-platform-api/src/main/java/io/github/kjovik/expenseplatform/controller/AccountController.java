package io.github.kjovik.expenseplatform.controller;


import io.github.kjovik.expenseplatform.dto.AccountResponse;
import io.github.kjovik.expenseplatform.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccountsDerivedBalances() {
        List<AccountResponse> accountResponses = accountService.getAccountsDerivedBalances();
        return ResponseEntity.ok(accountResponses);
    }
}
