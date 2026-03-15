package com.metropolia.aarojy.database_solutions_project.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_summary")
public class OrderSummary {

    @Column(name = "order_id")
    private int orderId;

    @Id
    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "status")
    private String status;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "amount_of_different_products")
    private Integer amountOfDifferentProducts;

    public OrderSummary() {
    }

    public OrderSummary(int orderId, int customerId, LocalDateTime orderDate, LocalDateTime deliveryDate, String status, BigDecimal totalCost, Integer amountOfDifferentProducts) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.totalCost = totalCost;
        this.amountOfDifferentProducts = amountOfDifferentProducts;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }


    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }


    public String getStatus() {
        return status;
    }


    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public Integer getAmountOfDifferentProducts() {
        return amountOfDifferentProducts;
    }
}
