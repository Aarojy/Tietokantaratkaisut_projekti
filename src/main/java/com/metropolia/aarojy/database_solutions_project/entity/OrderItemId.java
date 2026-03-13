package com.metropolia.aarojy.database_solutions_project.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class OrderItemId implements Serializable {

    private Integer orderId;
    private Integer productId;
}

