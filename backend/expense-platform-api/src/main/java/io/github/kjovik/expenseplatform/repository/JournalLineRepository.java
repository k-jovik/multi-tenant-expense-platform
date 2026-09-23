package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    List<JournalLine> findByAccountId(UUID id);


    @Query(
            "select jL.accountId as accountId, sum (jL.amountMinor) as balance " +
                    "from JournalLine jL " +
                    "where jL.accountId in (select a.id from Account a where a.tenantId = :tenantId) " +
                    "group by jL.accountId "
    )
    List<AccountBalanceProjection> sumByTenantId(@Param("tenantId") UUID tenantId);

}
