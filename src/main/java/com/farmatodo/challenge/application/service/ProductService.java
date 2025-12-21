package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.model.SearchHistory;
import com.farmatodo.challenge.domain.port.in.ProductUseCase;
import com.farmatodo.challenge.domain.port.out.InventoryPort;
import com.farmatodo.challenge.domain.port.out.SearchHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final InventoryPort inventoryPort;
    private final SearchHistoryPort searchHistoryPort;

    @Value("${app.business.product.min-stock-display:0}")
    private int minStockDisplay;

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
