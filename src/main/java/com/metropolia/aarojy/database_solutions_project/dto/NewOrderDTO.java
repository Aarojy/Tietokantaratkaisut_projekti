package com.metropolia.aarojy.database_solutions_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public record NewOrderDTO(

        @NotEmpty(message = "items cannot be empty")
        @Valid
        ArrayList<OrderItemRequest> items
) {

    public List<OrderItemRequest> getItems() {
        return items;
    }
}
