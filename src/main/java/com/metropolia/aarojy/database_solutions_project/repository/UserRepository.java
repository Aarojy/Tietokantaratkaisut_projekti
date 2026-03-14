package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Integer> {
     Optional<AppUser> findByUsername(String username);
}
