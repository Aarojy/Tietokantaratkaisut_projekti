package com.metropolia.aarojy.database_solutions_project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(

        @Email(message = "Invalid email format")
        @NotBlank
        String email
) {}

