package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.SupplierDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Product;
import com.metropolia.aarojy.database_solutions_project.entity.ProductCategory;
import com.metropolia.aarojy.database_solutions_project.repository.CategoryRepository;
import com.metropolia.aarojy.database_solutions_project.repository.SupplierRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<ProductCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .map(category -> ResponseEntity.ok(category.getProducts()))
                .orElse(ResponseEntity.notFound().build());
    }
}
