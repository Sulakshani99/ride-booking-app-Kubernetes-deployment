package com.ridebooking.auth.service;

import com.ridebooking.auth.dto.UserLookupResponse;
import com.ridebooking.auth.model.AppUser;
import com.ridebooking.auth.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLookupService {

    private final AppUserRepository appUserRepository;

    public UserLookupService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Optional<UserLookupResponse> findUserById(Long id) {
        return appUserRepository.findById(id)
                .map(this::toResponse);
    }

    private UserLookupResponse toResponse(AppUser user) {
        return new UserLookupResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}
