package com.metropolia.aarojy.database_solutions_project.dto;

import java.util.ArrayList;
import java.util.List;

public record NewOrderDTO(
        ArrayList<OrderItemRequest> items
) {

    public List<OrderItemRequest> getItems() {
        return items;
    }
}
