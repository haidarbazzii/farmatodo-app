package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.model.SearchHistory;
import com.farmatodo.challenge.domain.port.in.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String query) {
        return ResponseEntity.ok(productUseCase.searchProducts(query));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SearchHistory>> getHistory() {
        return ResponseEntity.ok(productUseCase.getSearchHistory());
    }
}