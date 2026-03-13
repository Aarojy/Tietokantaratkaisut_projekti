package com.metropolia.aarojy.database_solutions_project.dto;

import java.util.List;

public record CreateOrderRequest(
        Integer customerId,
        Integer shippingAddressId,
        List<OrderItemRequest> items
) {}

