// Úsalo SOLO si prefieres clases normales. Si usas el record de arriba, ignora esto.
package com.farmatodo.challenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
}