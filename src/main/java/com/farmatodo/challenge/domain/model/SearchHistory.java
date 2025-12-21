package com.farmatodo.challenge.domain.model;

import java.time.LocalDateTime;

public record SearchHistory(Long id, String query, LocalDateTime searchedAt) {}
