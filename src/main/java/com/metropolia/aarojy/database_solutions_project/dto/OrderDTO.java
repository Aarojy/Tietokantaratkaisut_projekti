package com.metropolia.aarojy.database_solutions_project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String status,
        CustomerAddressDTO shippingAddress,
        BigDecimal totalPrice,
        List<OrderItemDTO> items
) {}
