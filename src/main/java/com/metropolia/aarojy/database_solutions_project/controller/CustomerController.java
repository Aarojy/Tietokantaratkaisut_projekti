package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.CustomerDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import com.metropolia.aarojy.database_solutions_project.mapper.CustomerMapper;
import com.metropolia.aarojy.database_solutions_project.mapper.OrderMapper;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCustomerById() {

        Integer authenticatedUserId = (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Customer customer = customerRepository.findByAppUser_Id(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerDTO custDto = CustomerMapper.toCustomerDTO(customer);

        return ResponseEntity.ok(custDto);
    }
}
