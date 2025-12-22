package com.farmatodo.challenge.infrastructure.adapter;

import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.port.out.InventoryPort;
import com.farmatodo.challenge.infrastructure.persistence.entity.ProductEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryAdapter implements InventoryPort {

    private final ProductJpaRepository repository;

    @Override
    public boolean decreaseStock(Long productId, Integer quantity) {

        int updatedRows = repository.decreaseStock(productId, quantity);
        return updatedRows > 0;
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findByNameContaining(String name) {
        return repository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // Mapper
    private Product toDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }
}
