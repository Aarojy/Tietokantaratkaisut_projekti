package com.metropolia.aarojy.database_solutions_project.dto;

import java.util.ArrayList;
import java.util.List;

public record NewOrderDTO(
        int customerId,
        ArrayList<OrderItemRequest> items
) {
    public int getCustomerId() {
        return customerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }
}
