package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAppUser_Id(Integer userId);
}
