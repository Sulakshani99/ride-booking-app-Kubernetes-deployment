package com.ridebooking.auth.bootstrap;

import com.ridebooking.auth.model.AccountRole;
import com.ridebooking.auth.model.AppUser;
import com.ridebooking.auth.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String fullName;
    private final String email;
    private final String password;
    private final String phoneNumber;

    public AdminAccountSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.full-name}") String fullName,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.phone-number}") String phoneNumber
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhoneNumber = phoneNumber.trim();

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail) || appUserRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            return;
        }

        AppUser admin = new AppUser(
                fullName.trim(),
                normalizedEmail,
            normalizedPhoneNumber,
                passwordEncoder.encode(password),
                AccountRole.ADMIN
        );

        appUserRepository.save(admin);
        log.info("Default admin account created for {}", normalizedEmail);
    }
}