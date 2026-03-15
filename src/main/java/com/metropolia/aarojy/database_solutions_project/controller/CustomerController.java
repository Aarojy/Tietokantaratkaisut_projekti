package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.CustomerAddressDTO;
import com.metropolia.aarojy.database_solutions_project.dto.CustomerDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import com.metropolia.aarojy.database_solutions_project.entity.CustomerAddress;
import com.metropolia.aarojy.database_solutions_project.mapper.CustomerMapper;
import com.metropolia.aarojy.database_solutions_project.mapper.OrderMapper;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerAddressRepository;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerController(CustomerRepository customerRepository, CustomerAddressRepository customerAddressRepository) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
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

    @GetMapping("/address")
    public ResponseEntity<?> getCustomerAddress() {

        Integer authenticatedUserId = (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Customer customer = customerRepository.findByAppUser_Id(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerAddress customerAddress = customerAddressRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer address not found"));

        CustomerAddressDTO custAddrDto = CustomerMapper.toCustomerAddressDTO(customerAddress);

        return ResponseEntity.ok(custAddrDto);
    }

    @PatchMapping("/address")
    public ResponseEntity<?> updateCustomerAddress(@Valid @RequestBody CustomerAddressDTO custAddrDto) {

        Integer authenticatedUserId = (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Customer customer = customerRepository.findByAppUser_Id(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerAddress customerAddress = customerAddressRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer address not found"));

        customerAddress.setStreet(custAddrDto.street());
        customerAddress.setCity(custAddrDto.city());
        customerAddress.setPostalCode(custAddrDto.postalCode());
        customerAddress.setCountry(custAddrDto.country());

        customerAddressRepository.save(customerAddress);

        return ResponseEntity.ok("Customer address updated successfully");
    }
}
