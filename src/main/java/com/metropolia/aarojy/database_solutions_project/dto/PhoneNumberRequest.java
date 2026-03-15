package com.metropolia.aarojy.database_solutions_project.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberRequest(

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
        @NotBlank
        String phoneNumber
) {}

