package com.mim736.qvs.service;

import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.security.JwtService;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.dto.AuthResponse;
import com.mim736.qvs.web.dto.LoginRequest;
import com.mim736.qvs.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken");
        }
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }
        Role role = request.getRole() == null ? Role.VERIFIER : request.getRole();
        if (role == Role.ADMIN) {
            throw new BusinessException("Admin accounts cannot be self-registered");
        }
        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername().trim());
        account.setEmail(request.getEmail().trim().toLowerCase());
        account.setFullName(request.getFullName().trim());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(role);
        account.setEnabled(true);
        userAccountRepository.save(account);
        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new AuthResponse(token, account.getUsername(), account.getFullName(), account.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new AuthResponse(token, account.getUsername(), account.getFullName(), account.getRole());
    }
}
