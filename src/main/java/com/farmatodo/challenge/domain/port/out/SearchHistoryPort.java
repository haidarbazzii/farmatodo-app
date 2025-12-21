package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.SearchHistory;

import java.util.List;

public interface SearchHistoryPort {
    void saveSearchHistory(String query);

    List<SearchHistory> findAllHistory();
}
