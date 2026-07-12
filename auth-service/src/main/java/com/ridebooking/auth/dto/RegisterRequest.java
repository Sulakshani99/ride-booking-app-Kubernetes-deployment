package com.ridebooking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(

        @NotBlank @Size(min = 2, max = 120) String fullName,

        @NotBlank @Email String email,

        @NotBlank
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits.")
        String phoneNumber,

        @NotBlank @Size(min = 6, max = 120) String password
) {
}