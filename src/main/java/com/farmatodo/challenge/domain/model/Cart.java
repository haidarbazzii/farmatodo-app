package com.farmatodo.challenge.domain.model;

import com.farmatodo.challenge.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private Long id;
    private Customer customer;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void addItem(Long productId, int quantity, int currStock) {
        if (currStock < quantity){
            throw new IllegalArgumentException("No hay suficiente stock para ID: " + productId);
        }
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            if (item.getQuantity() + quantity <= currStock) {
                item.setQuantity(item.getQuantity() + quantity);
            }
            else {
                throw new IllegalArgumentException("No hay suficiente stock para ID: " + productId);
            }
        } else {
            items.add(new CartItem(productId, quantity));
        }
    }
}