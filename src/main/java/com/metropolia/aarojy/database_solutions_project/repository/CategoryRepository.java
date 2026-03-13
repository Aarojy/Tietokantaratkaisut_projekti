package com.metropolia.aarojy.database_solutions_project.repository;

import com.metropolia.aarojy.database_solutions_project.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<ProductCategory, Integer> {
}
