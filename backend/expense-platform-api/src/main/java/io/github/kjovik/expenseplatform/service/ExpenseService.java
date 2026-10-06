package io.github.kjovik.expenseplatform.service;

import io.github.kjovik.expenseplatform.context.RlsContext;
import io.github.kjovik.expenseplatform.context.TenantContext;
import io.github.kjovik.expenseplatform.dto.ExpenseRequest;
import io.github.kjovik.expenseplatform.dto.ExpenseResponse;
import io.github.kjovik.expenseplatform.entity.*;
import io.github.kjovik.expenseplatform.enums.EntryType;
import io.github.kjovik.expenseplatform.enums.ExpenseStatus;
import io.github.kjovik.expenseplatform.exception.ConflictException;
import io.github.kjovik.expenseplatform.exception.ResourceNotFoundException;
import io.github.kjovik.expenseplatform.repository.AccountRepository;
import io.github.kjovik.expenseplatform.repository.ExpenseRepository;
import io.github.kjovik.expenseplatform.repository.JournalEntryRepository;
import io.github.kjovik.expenseplatform.repository.JournalLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;
    private final RlsContext rlsContext;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest expenseRequest) {
        Expense expense = new Expense();
        UUID tenantId = TenantContext.getTenantId();
        expense.setTenantId(tenantId);
        rlsContext.setTenant(tenantId);
        UUID userID = TenantContext.getUserId();
        expense.setUserId(userID);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setAmountMinor(expenseRequest.amountMinor());
        expense.setCategory(expenseRequest.category());
        if (expenseRequest.description() != null) {
            expense.setDescription(expenseRequest.description());
        }
        expense.setCurrency(expenseRequest.currency());
        expense.setSubmittedAt(Instant.now());

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.from(saved);
    }

    @Transactional (readOnly = true)
    public List<ExpenseResponse> list(){
        UUID tenantId = TenantContext.getTenantId();
        rlsContext.setTenant(tenantId);
        return expenseRepository.findByTenantId(tenantId).stream().map(ExpenseResponse::from).toList();
    }

    @Transactional (readOnly = true)
    public List<ExpenseResponse> listPendingExpenses(){
        UUID tenantId = TenantContext.getTenantId();
        rlsContext.setTenant(tenantId);
        List<Expense> expenses = expenseRepository.findByTenantIdAndStatus(tenantId,ExpenseStatus.SUBMITTED);
        return expenses.stream().map(ExpenseResponse::from).toList();
    }


    @Transactional (isolation = Isolation.SERIALIZABLE)
    public ExpenseResponse approve(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        rlsContext.setTenant(tenantId);
        Expense expense = expenseRepository.findByTenantIdAndId(tenantId,id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (expense.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new ConflictException("Only submitted expenses can be approved");
        }
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedById(TenantContext.getUserId());
        expense.setApprovedAt(Instant.now());
        Expense saved = expenseRepository.save(expense);

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setEntryType(EntryType.APPROVAL);
        journalEntry.setExpenseId(saved.getId());
        journalEntry.setTenantId(tenantId);
        journalEntry = journalEntryRepository.save(journalEntry);


        JournalLine debitLine = new JournalLine();
        JournalLine creditLine = new JournalLine();
        debitLine.setJournalEntryId(journalEntry.getId());
        creditLine.setJournalEntryId(journalEntry.getId());

        Account expenseAccount = accountRepository.findByTenantIdAndName(tenantId,"Expense:" + saved.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Account payableAccount = accountRepository.findByTenantIdAndName(tenantId,"Payable:" + saved.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        debitLine.setAccountId(expenseAccount.getId());
        creditLine.setAccountId(payableAccount.getId());

        debitLine.setAmountMinor(saved.getAmountMinor());
        creditLine.setAmountMinor(-saved.getAmountMinor());

        journalLineRepository.save(debitLine);
        journalLineRepository.save(creditLine);

        return ExpenseResponse.from(saved);
    }

    @Transactional (isolation = Isolation.SERIALIZABLE)
    public ExpenseResponse reject(UUID expenseId, String reason){
        UUID tenantId = TenantContext.getTenantId();
        rlsContext.setTenant(tenantId);
        Expense expense = expenseRepository.findByTenantIdAndId(tenantId,expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (expense.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new ConflictException("Only submitted expenses can be rejected");
        }
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setRejectionReason(reason);
        expense = expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }



    @Transactional (isolation = Isolation.SERIALIZABLE)
    public ExpenseResponse reverse(UUID id, String reason){
        UUID tenantId = TenantContext.getTenantId();
        UUID userId = TenantContext.getUserId();
        rlsContext.setTenant(tenantId);
        Expense expense = expenseRepository.findByTenantIdAndId(tenantId,id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new ConflictException("Only approved expenses can be reversed");
        }
        JournalEntry entry = journalEntryRepository.findByExpenseIdAndEntryType(expense.getId(),EntryType.APPROVAL)
                        .orElseThrow(() -> new ResourceNotFoundException("Original journal entry not found"));

        expense.setStatus(ExpenseStatus.REVERSED);
        expense.setReversedById(userId);
        expense.setReversedAt(Instant.now());
        expense.setReversalReason(reason);
        Expense saved = expenseRepository.save(expense);

        JournalEntry newEntry = new JournalEntry();
        newEntry.setReversesEntryId(entry.getId());
        newEntry.setEntryType(EntryType.REVERSAL);
        newEntry.setExpenseId(saved.getId());
        newEntry.setTenantId(tenantId);
        journalEntryRepository.save(newEntry);


        List<JournalLine> oldJournalLines = journalLineRepository.findByJournalEntryId(entry.getId());
        for (JournalLine journalLine : oldJournalLines) {
            JournalLine reversed = new JournalLine();
            reversed.setJournalEntryId(newEntry.getId());
            reversed.setAccountId(journalLine.getAccountId());
            reversed.setAmountMinor(-journalLine.getAmountMinor());
            journalLineRepository.save(reversed);
        }

        return  ExpenseResponse.from(saved);
    }
}
