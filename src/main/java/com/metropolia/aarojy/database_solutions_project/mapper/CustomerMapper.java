package com.metropolia.aarojy.database_solutions_project.mapper;

import com.metropolia.aarojy.database_solutions_project.dto.CustomerAddressDTO;
import com.metropolia.aarojy.database_solutions_project.dto.CustomerDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import com.metropolia.aarojy.database_solutions_project.entity.CustomerAddress;

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

    public static CustomerAddressDTO toCustomerAddressDTO(CustomerAddress customerAddress) {
        return new CustomerAddressDTO(
                customerAddress.getStreet(),
                customerAddress.getCity(),
                customerAddress.getPostalCode(),
                customerAddress.getCountry()
        );
    }
}
