package com.ridebooking.auth.controller;

import com.ridebooking.auth.dto.LoginRequest;
import com.ridebooking.auth.dto.LoginResponse;
import com.ridebooking.auth.dto.LogoutResponse;
import com.ridebooking.auth.dto.CreateDriverRequest;
import com.ridebooking.auth.dto.RegisterRequest;
import com.ridebooking.auth.dto.RegisterResponse;
import com.ridebooking.auth.dto.UserLookupResponse;
import com.ridebooking.auth.service.AuthService;
import com.ridebooking.auth.service.UserLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserLookupService userLookupService;

    public AuthController(AuthService authService, UserLookupService userLookupService) {
        this.authService = authService;
        this.userLookupService = userLookupService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/admin/drivers/create")
    public ResponseEntity<RegisterResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createDriver(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(authService.logout(authorizationHeader));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserLookupResponse> getUserById(
        @PathVariable Long id )
    {
        return ResponseEntity.ok(userLookupService.findUserById(id).orElse(null));
    }
}
