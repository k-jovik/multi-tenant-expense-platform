package io.github.kjovik.expenseplatform.service;

import io.github.kjovik.expenseplatform.dto.AuthResponse;
import io.github.kjovik.expenseplatform.dto.LoginRequest;
import io.github.kjovik.expenseplatform.dto.RegisterRequest;
import io.github.kjovik.expenseplatform.entity.Account;
import io.github.kjovik.expenseplatform.entity.Tenant;
import io.github.kjovik.expenseplatform.entity.User;
import io.github.kjovik.expenseplatform.enums.AccountType;
import io.github.kjovik.expenseplatform.enums.Role;
import io.github.kjovik.expenseplatform.exception.ConflictException;
import io.github.kjovik.expenseplatform.exception.InvalidCredentialsException;
import io.github.kjovik.expenseplatform.repository.AccountRepository;
import io.github.kjovik.expenseplatform.repository.TenantRepository;
import io.github.kjovik.expenseplatform.repository.UserRepository;
import io.github.kjovik.expenseplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {

        if (tenantRepository.findByName(registerRequest.tenantName()).isPresent()){
            throw new ConflictException("Tenant name already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(registerRequest.tenantName());
        tenant = tenantRepository.save(tenant);
        User user = new User();
        user.setEmail(registerRequest.email());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        user.setTenantId(tenant.getId());
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);

        seedAccounts(tenant.getId(),user.getId());

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getRole().name(),tenant.getId());
    }

    private void seedAccounts(UUID tenantId, UUID userId) {
        saveAccount(tenantId,"Cash",AccountType.CASH);
        saveAccount(tenantId,"Expense:Meals",AccountType.EXPENSE);
        saveAccount(tenantId,"Expense:Travel",AccountType.EXPENSE);
        saveAccount(tenantId,"Expense:Software",AccountType.EXPENSE);
        saveAccount(tenantId,"Expense:Equipment",AccountType.EXPENSE);
        saveAccount(tenantId,"Payable:" + userId,AccountType.PAYABLE);
    }

    private void saveAccount (UUID tenantId, String name, AccountType type) {
        Account account = new Account();
        account.setTenantId(tenantId);
        account.setName(name);
        account.setType(type);
        accountRepository.save(account);
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Tenant tenant = tenantRepository.findByName(loginRequest.tenantName())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));
        User user = userRepository.findByTenantIdAndEmail(tenant.getId(),loginRequest.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));

        String pass = user.getPasswordHash();

        if (!passwordEncoder.matches(loginRequest.password(), pass)){
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getRole().name(),tenant.getId());
    }
}
