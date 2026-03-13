package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {
    Optional<CustomerAddress> findByCustomerId(int customerId);
}
