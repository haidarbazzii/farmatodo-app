package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.application.service.CartService;
import com.farmatodo.challenge.domain.model.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@RequestBody AddItemRequest request) {
        return ResponseEntity.ok(
                cartService.addItemToCart(request.email(), request.productId(), request.quantity())
        );
    }

    public record AddItemRequest(String email, Long productId, int quantity) {}
}