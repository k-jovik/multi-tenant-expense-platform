package io.github.kjovik.expenseplatform.service;


import io.github.kjovik.expenseplatform.context.TenantContext;
import io.github.kjovik.expenseplatform.dto.AccountResponse;
import io.github.kjovik.expenseplatform.entity.Account;
import io.github.kjovik.expenseplatform.repository.AccountBalanceProjection;
import io.github.kjovik.expenseplatform.repository.AccountRepository;
import io.github.kjovik.expenseplatform.repository.JournalLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JournalLineRepository journalLineRepository;

    @Transactional (readOnly = true)
    public List<AccountResponse> getAccountsDerivedBalances(){
        UUID tenantId = TenantContext.getTenantId();
        List<Account> accounts = accountRepository.findByTenantId(tenantId);
        Map<UUID,Long> balances = journalLineRepository.sumByTenantId(tenantId)
                .stream().collect(Collectors.toMap(
                        AccountBalanceProjection::getAccountId,
                        AccountBalanceProjection::getBalance
                ));
        return accounts.stream().map(
                a -> AccountResponse.from(a,balances.getOrDefault(a.getId(),0L))
        ).toList();
    }
}
