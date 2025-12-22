package com.farmatodo.challenge.infrastructure.persistence.repository;

import com.farmatodo.challenge.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    // =================================================================================
    // SOLUCIÓN DE CONCURRENCIA (Locking Implícito)
    // =================================================================================
    // Esta query es atómica a nivel de base de datos.
    // UPDATE products SET stock = stock - X WHERE id = Y AND stock >= X
    // Retorna 1 si se actualizó (había stock), 0 si no (no había stock o no existe).
    // Esto previene la "Race Condition" sin bloquear la tabla completa.
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") Integer qty);

    Optional<ProductEntity> findByName(String name);
}
