package com.metropolia.aarojy.database_solutions_project.dto;

public record LoginDTO(
        String username,
        String password
) {
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
