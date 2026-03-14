package com.metropolia.aarojy.database_solutions_project.mapper;

import com.metropolia.aarojy.database_solutions_project.dto.CustomerDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Customer;

public class CustomerMapper {

    private CustomerMapper () {

    }

    public static CustomerDTO toCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getFirst_name(),
                customer.getLast_name(),
                customer.getEmail(),
                customer.getPhone()
        );
    }
}
