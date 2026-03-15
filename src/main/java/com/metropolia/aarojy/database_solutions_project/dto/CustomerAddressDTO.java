package com.metropolia.aarojy.database_solutions_project.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerAddressDTO(
        @NotBlank(message = "Street cannot be empty")
        String street,

        @NotBlank(message = "City cannot be empty")
        String city,

        @NotBlank(message = "Postal code cannot be empty")
        String postalCode,

        @NotBlank(message = "Country cannot be empty")
        String country
) {}


