package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface InventoryPort {
    Optional<Product> findById(Long id);
    List<Product> findByNameContaining(String name);

    // Devuelve true si logró restar stock en la DB
    // Evita condiciones de carrera
    boolean decreaseStock(Long productId, Integer quantity);

    List<Product> findAll();
    Product save(Product product);
    Optional<Product> findByName(String name);
}
