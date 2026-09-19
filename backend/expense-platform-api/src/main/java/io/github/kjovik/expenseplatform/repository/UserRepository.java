package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);
}
