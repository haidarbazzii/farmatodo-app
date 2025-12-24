package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.application.service.OrderService;
import com.farmatodo.challenge.application.service.ProductService;
import com.farmatodo.challenge.application.service.TokenizationService;
import com.farmatodo.challenge.infrastructure.persistence.entity.ProductEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.ProductJpaRepository; // Usamos JPA directo para rapidez en admin
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final ProductJpaRepository productRepository;
    private final ProductService productService;
    private final TokenizationService tokenService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "paymentRejectionProbability", orderService.getPaymentRejectionProbability(),
                "tokenRejectionProbability", tokenService.getRejectionProbability(), // <--- Nuevo campo
                "maxRetries", orderService.getMaxRetries(),
                "minStockDisplay", productService.getMinStockDisplay()
        ));
    }

    // 2. Cambiar Probabilidad de Fallo (0.0 a 1.0)
    @PostMapping("/config/rejection-probability")
    public ResponseEntity<String> setRejectionProbability(@RequestParam double value) {
        orderService.setPaymentRejectionProbability(value);
        return ResponseEntity.ok("Probabilidad de rechazo actualizada a: " + value);
    }

    // 3. Reabastecer Stock de un Producto
    @PostMapping("/products/{id}/restock")
    public ResponseEntity<String> restockProduct(@PathVariable Long id, @RequestParam int quantity) {
        System.out.println("Estas por aca");
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        return ResponseEntity.ok("Stock actualizado. Nuevo stock para " + product.getName() + ": " + product.getStock());
    }

    // 2. Modificar Reintentos (OrderService)
    @PostMapping("/config/max-retries")
    public ResponseEntity<String> setMaxRetries(@RequestParam int value) {
        orderService.setMaxRetries(value);
        return ResponseEntity.ok("Reintentos de pago actualizados a: " + value);
    }

    // 3. Modificar Stock Mínimo Visible (ProductService)
    @PostMapping("/config/min-stock-display")
    public ResponseEntity<String> setMinStockDisplay(@RequestParam int value) {
        productService.setMinStockDisplay(value);
        return ResponseEntity.ok("Productos con stock menor a " + value + " ahora estarán ocultos.");
    }

    @PostMapping("/config/token-rejection-probability")
    public ResponseEntity<String> setTokenRejectionProbability(@RequestParam double value) {
        tokenService.setTokenRejectionProbability(value);
        return ResponseEntity.ok("Probabilidad de fallo en tokenización actualizada a: " + value);
    }
}