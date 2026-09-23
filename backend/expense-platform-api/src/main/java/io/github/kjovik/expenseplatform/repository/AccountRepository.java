package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByTenantIdAndName(UUID uuid, String name);
}
