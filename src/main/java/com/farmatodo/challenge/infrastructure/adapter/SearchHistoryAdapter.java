package com.farmatodo.challenge.infrastructure.adapter;

import com.farmatodo.challenge.domain.model.SearchHistory;
import com.farmatodo.challenge.domain.port.out.SearchHistoryPort;
import com.farmatodo.challenge.infrastructure.persistence.entity.SearchHistoryEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.SearchHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchHistoryAdapter implements SearchHistoryPort {

    private final SearchHistoryJpaRepository repository;

    @Override
    @Async // Almacena de manera asincrona
    public void saveSearchHistory(String query) {
        log.info("Saving search history asynchronously in thread: {}", Thread.currentThread().getName());
        repository.save(new SearchHistoryEntity(query));
    }
    @Override
    public List<SearchHistory> findAllHistory() {

        //Solo retorna 20 busquedas
        return repository.findAllByOrderBySearchedAtDesc().stream()
                .map(entity -> new SearchHistory(
                        entity.getId(),
                        entity.getQuery(),
                        entity.getSearchedAt()
                ))
                .limit(20).collect(Collectors.toList());
    }
}