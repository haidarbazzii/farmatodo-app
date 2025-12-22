package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.model.SearchHistory;
import com.farmatodo.challenge.domain.port.in.ProductUseCase;
import com.farmatodo.challenge.domain.port.out.InventoryPort;
import com.farmatodo.challenge.domain.port.out.SearchHistoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final InventoryPort inventoryPort;
    private final SearchHistoryPort searchHistoryPort;

    @Value("${app.business.product.min-stock-display:0}")
    private int minStockDisplay;

    public List<Product> getAllAvailableProducts() {
        // Filtramos usando la variable dinámica
        return inventoryPort.findAll().stream()
                .filter(p -> p.getStock() > minStockDisplay)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE GESTIÓN (ADMIN) ---

    // Setter para modificar en caliente
    public void setMinStockDisplay(int newThreshold) {
        if (newThreshold < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        this.minStockDisplay = newThreshold;
        log.info("ADMIN: Umbral de visualización de stock actualizado a: > {}", newThreshold);
    }

    @Override
    public List<Product> searchProducts(String query) {
        // 1. Registro asíncrono (No bloquea la respuesta)
        searchHistoryPort.saveSearchHistory(query);

        // 2. Búsqueda y Filtrado
        return inventoryPort.findByNameContaining(query).stream()
                .filter(product -> product.hasStock(minStockDisplay))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchHistory> getSearchHistory() {
        return searchHistoryPort.findAllHistory();
    }
}
