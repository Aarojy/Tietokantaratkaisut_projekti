package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.entity.Product;
import com.metropolia.aarojy.database_solutions_project.entity.ProductCategory;
import com.metropolia.aarojy.database_solutions_project.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryRepository CategoryRepository;

    public CategoryController(CategoryRepository CategoryRepository) {
        this.CategoryRepository = CategoryRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<ProductCategory>> getAllCategories() {
        return ResponseEntity.ok(CategoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable Integer id) {
        return CategoryRepository.findById(id)
                .map(category -> ResponseEntity.ok(category.getProducts()))
                .orElse(ResponseEntity.notFound().build());
    }
}
