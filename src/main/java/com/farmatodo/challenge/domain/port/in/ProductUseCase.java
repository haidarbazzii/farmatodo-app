package com.farmatodo.challenge.domain.port.in;

import com.farmatodo.challenge.domain.model.Product;
import com.farmatodo.challenge.domain.model.SearchHistory;

import java.util.List;

public interface ProductUseCase {
    // Búsqueda con historial asíncrono y filtro de stock
    List<Product> searchProducts(String query);

    List<SearchHistory> getSearchHistory();
}