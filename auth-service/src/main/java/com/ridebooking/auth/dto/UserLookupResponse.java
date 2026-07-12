package com.ridebooking.auth.dto;

import com.ridebooking.auth.model.AccountRole;

public record UserLookupResponse(
        Long id,
        String email,
        String fullName,
        AccountRole role
) {}
