package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.application.service.ProductService;
import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.model.SearchHistory;
import com.farmatodo.challenge.domain.port.in.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String query) {
        return ResponseEntity.ok(productUseCase.searchProducts(query));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllAvailableProducts());
    }

    @GetMapping("/history")
    public ResponseEntity<List<SearchHistory>> getHistory() {
        return ResponseEntity.ok(productUseCase.getSearchHistory());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
}