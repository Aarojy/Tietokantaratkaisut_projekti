package com.metropolia.aarojy.database_solutions_project.dto;

public record RegisterDTO(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String phone
) {
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
