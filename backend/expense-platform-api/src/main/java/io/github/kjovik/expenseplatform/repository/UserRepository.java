package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
