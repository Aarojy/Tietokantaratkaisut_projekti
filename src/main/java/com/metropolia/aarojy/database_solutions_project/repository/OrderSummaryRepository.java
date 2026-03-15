package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.Order;
import com.metropolia.aarojy.database_solutions_project.entity.OrderSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderSummaryRepository extends JpaRepository<OrderSummary, Long> {
    List<OrderSummary> findByCustomerId(Integer customerId);
}

