package com.metropolia.aarojy.database_solutions_project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        int order_id,
        BigDecimal total_cost,
        int amount_of_different_products,
        String status,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate
) {}
