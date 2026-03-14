package com.metropolia.aarojy.database_solutions_project.mapper;

import com.metropolia.aarojy.database_solutions_project.dto.*;
import com.metropolia.aarojy.database_solutions_project.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class OrderMapper {
    private OrderMapper() {}

    public static OrderDTO toOrderDTO(Order order) {

        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return new OrderDTO(
                order.getId(),
                order.getOrder_date(),
                order.getDelivery_date(),
                order.getStatus(),

                new CustomerAddressDTO(
                        order.getShippingAddress().getStreet(),
                        order.getShippingAddress().getCity(),
                        order.getShippingAddress().getPostalCode(),
                        order.getShippingAddress().getCountry()
                ),

                total,
                order.getItems().stream()
                        .map(OrderMapper::toOrderItemDTO)
                        .collect(Collectors.toList())
        );
    }

    private static OrderItemDTO toOrderItemDTO(OrderItem item) {
        return new OrderItemDTO(
                item.getQuantity(),
                toProductDTO(item.getProduct())
        );
    }

    public static ProductDTO toProductDTO(Product p) {
        return new ProductDTO(
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                new CategoryDTO(p.getCategory().getName())
        );
    }

    public static Order toOrderEntity(Customer customer, CustomerAddress shippingAddress) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrder_date(LocalDateTime.now());
        order.setStatus("NEW");
        order.setDelivery_date(null);
        order.setShippingAddress(shippingAddress);
        return order;
    }
}