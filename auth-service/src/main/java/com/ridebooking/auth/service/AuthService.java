package com.ridebooking.auth.service;

import com.ridebooking.auth.dto.*;
import com.ridebooking.auth.model.AccountRole;
import com.ridebooking.auth.model.AppUser;
import com.ridebooking.auth.exception.AccountAlreadyExistsException;
import com.ridebooking.auth.exception.AuthenticationFailedException;
import com.ridebooking.auth.repository.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        return createAccount(request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.password(),
                AccountRole.USER, "Registration successful. Please login to get tokens.");
    }

    @Transactional
    public RegisterResponse createDriver(CreateDriverRequest request) {
        return createAccount(request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.password(),
                AccountRole.DRIVER,
                "Driver account created successfully.");
    }

    private RegisterResponse createAccount(
            String fullName,
            String email,
            String phoneNumber,
            String password,
            AccountRole role,
            String successMessage
    ) {
        String normalizedEmail = email.trim().toLowerCase();

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new AccountAlreadyExistsException("Email is already registered");
        }
        if (appUserRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AccountAlreadyExistsException("Phone number is already registered");
        }

        AppUser user = new AppUser(
                fullName,
                normalizedEmail,
                phoneNumber,
                passwordEncoder.encode(password),
                role
        );
        AppUser savedUser = appUserRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getRole().name(),
                successMessage
        );
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            AppUser user = (AppUser) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new LoginResponse(
                    "Bearer",
                    accessToken,
                    refreshToken,
                    user.getRole().name(),
                    "local"
            );
        } catch (AuthenticationException ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    public LogoutResponse logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring("Bearer ".length());
        try {
            jwtService.invalidateUserTokens(token);
        } catch (Exception ignored) {
            return null;
        }
        return new LogoutResponse("Logged out successfully.");
    }
}
