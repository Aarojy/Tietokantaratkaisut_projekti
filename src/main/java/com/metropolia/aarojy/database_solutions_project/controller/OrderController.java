package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.NewOrderDTO;
import com.metropolia.aarojy.database_solutions_project.dto.OrderDTO;
import com.metropolia.aarojy.database_solutions_project.dto.OrderItemRequest;
import com.metropolia.aarojy.database_solutions_project.entity.*;
import com.metropolia.aarojy.database_solutions_project.mapper.OrderMapper;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerAddressRepository;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerRepository;
import com.metropolia.aarojy.database_solutions_project.repository.OrderRepository;
import com.metropolia.aarojy.database_solutions_project.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, CustomerAddressRepository customerAddressRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Integer userId) {

        List<Order> orders = orderRepository.findByCustomerId(userId);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderMapper::toOrderDTO)
                .toList();

        return ResponseEntity.ok(orderDTOs);
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody NewOrderDTO newOrderDTO) {

        CustomerAddress shippingAddress = customerAddressRepository
                .findByCustomerId(newOrderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        Customer customer = customerRepository
                .findById(newOrderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = OrderMapper.toOrderEntity(customer, shippingAddress);

        for (OrderItemRequest itemDTO : newOrderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setOrder(order);
            item.setUnitPrice(product.getPrice());

            order.getItems().add(item);
        }

        Order savedOrder = orderRepository.save(order);

        OrderDTO response = OrderMapper.toOrderDTO(savedOrder);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (Objects.equals(order.getStatus(), "CANCELLED")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error", "Order already cancelled",
                            "message", "This order cannot be cancelled again."
                    )
            );
        }

        if (Objects.equals(order.getStatus(), "SHIPPED")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error", "Order already shipped",
                            "message", "Shipped orders cannot be cancelled."
                    )
            );
        }

        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);

        return ResponseEntity.ok(OrderMapper.toOrderDTO(cancelledOrder));
    }
}
