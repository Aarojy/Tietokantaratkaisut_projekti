package com.metropolia.aarojy.database_solutions_project.dto;

import java.math.BigDecimal;

public record ProductDTO(
        String name,
        String description,
        BigDecimal price,
        String category,
        String supplier
) {}
