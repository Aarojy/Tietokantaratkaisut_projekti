package com.metropolia.aarojy.database_solutions_project.dto;

public record CustomerAddressDTO(
    String street,
    String city,
    String postalCode,
    String country
) {}

