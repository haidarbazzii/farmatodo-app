package com.farmatodo.challenge.infrastructure.persistence.repository;
import com.farmatodo.challenge.infrastructure.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CardTokenJpaRepository extends JpaRepository<CardTokenEntity, UUID> {}
