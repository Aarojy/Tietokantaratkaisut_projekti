package com.metropolia.aarojy.database_solutions_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        int productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {
    public int getId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}