package io.github.kjovik.expenseplatform.service;

import io.github.kjovik.expenseplatform.dto.AuthResponse;
import io.github.kjovik.expenseplatform.dto.LoginRequest;
import io.github.kjovik.expenseplatform.dto.RegisterRequest;
import io.github.kjovik.expenseplatform.entity.Tenant;
import io.github.kjovik.expenseplatform.entity.User;
import io.github.kjovik.expenseplatform.enums.Role;
import io.github.kjovik.expenseplatform.repository.TenantRepository;
import io.github.kjovik.expenseplatform.repository.UserRepository;
import io.github.kjovik.expenseplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {

        if (tenantRepository.findByName(registerRequest.tenantName()).isPresent()){
            throw new RuntimeException("Tenant name already exists");
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
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getRole().name(),tenant.getId());
    }


    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Tenant tenant = tenantRepository.findByName(loginRequest.tenantName())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));
        User user = userRepository.findByTenantIdAndEmail(tenant.getId(),loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        String pass = user.getPasswordHash();

        if (!passwordEncoder.matches(loginRequest.password(), pass)){
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getRole().name(),tenant.getId());
    }
}
