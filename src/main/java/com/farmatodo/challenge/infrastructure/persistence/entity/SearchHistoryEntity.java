package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
public class SearchHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String query;
    private LocalDateTime searchedAt;

    public SearchHistoryEntity(String query) {
        this.query = query;
        this.searchedAt = LocalDateTime.now();
    }
}
