package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.NewOrderDTO;
import com.metropolia.aarojy.database_solutions_project.dto.OrderDTO;
import com.metropolia.aarojy.database_solutions_project.dto.OrderItemRequest;
import com.metropolia.aarojy.database_solutions_project.entity.*;
import com.metropolia.aarojy.database_solutions_project.mapper.OrderMapper;
import com.metropolia.aarojy.database_solutions_project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, CustomerAddressRepository customerAddressRepository, CustomerRepository customerRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId() {

        Integer authenticatedUserId = (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Customer customer = customerRepository.findByAppUser_Id(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Order> orders = orderRepository.findByCustomerId(customer.getId());

        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderMapper::toOrderDTO)
                .toList();

        return ResponseEntity.ok(orderDTOs);
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody NewOrderDTO newOrderDTO) {

        Integer authenticatedUserId = (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Customer customer = customerRepository.findByAppUser_Id(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        int customerId = customer.getId();

        CustomerAddress shippingAddress = customerAddressRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        Order order = OrderMapper.toOrderEntity(customer, shippingAddress);

        for (OrderItemRequest itemDTO : newOrderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setReserved_quantity(product.getReserved_quantity() + itemDTO.getQuantity());
            productRepository.save(product);

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

    @Transactional
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {

        int user_id = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        Customer customer = customerRepository.findByAppUser_Id(user_id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getCustomer().getId() != customer.getId()) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error", "Forbidden",
                            "message", "You do not have permission to cancel this order."
                    )
            );
        }

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

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            product.setReserved_quantity(
                    product.getReserved_quantity() - item.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);

        return ResponseEntity.ok(OrderMapper.toOrderDTO(cancelledOrder));
    }
}
