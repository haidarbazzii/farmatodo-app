package com.farmatodo.challenge.infrastructure.persistence.repository;

import com.farmatodo.challenge.infrastructure.persistence.entity.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistoryEntity, Long> {

    // Query Method para ordenar por fecha descendente (lo más nuevo primero)
    List<SearchHistoryEntity> findAllByOrderBySearchedAtDesc();
}