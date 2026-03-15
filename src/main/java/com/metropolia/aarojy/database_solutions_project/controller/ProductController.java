package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.dto.ProductDTO;
import com.metropolia.aarojy.database_solutions_project.entity.Product;
import com.metropolia.aarojy.database_solutions_project.mapper.OrderMapper;
import com.metropolia.aarojy.database_solutions_project.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .map(OrderMapper::toProductDTO)
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return productRepository.findById(id)
                .map(OrderMapper::toProductDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name/{string}")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@PathVariable String string) {
        List<ProductDTO> products = productRepository.findByNameContainingIgnoreCase(string)
                .stream()
                .map(OrderMapper::toProductDTO)
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/price/{min}/{max}")
    public ResponseEntity<List<ProductDTO>> searchProductsByPriceRange(@PathVariable Double min, @PathVariable Double max) {
        List<ProductDTO> products = productRepository.findAll()
                .stream()
                .filter(p -> p.getPrice().doubleValue() >= min && p.getPrice().doubleValue() <= max)
                .map(OrderMapper::toProductDTO)
                .toList();

        return ResponseEntity.ok(products);
    }
}
