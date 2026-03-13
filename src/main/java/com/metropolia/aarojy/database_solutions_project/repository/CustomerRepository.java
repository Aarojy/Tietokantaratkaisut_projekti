package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
