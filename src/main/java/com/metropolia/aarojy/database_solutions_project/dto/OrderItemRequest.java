package com.metropolia.aarojy.database_solutions_project.dto;

public record OrderItemRequest(
        int productId,
        int quantity
) {
    public int getId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
