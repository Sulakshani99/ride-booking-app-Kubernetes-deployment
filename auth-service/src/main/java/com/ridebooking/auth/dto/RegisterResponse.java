package com.ridebooking.auth.dto;

public record RegisterResponse(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        String role,
        String message
) {
}