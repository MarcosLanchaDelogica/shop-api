package com.example.shop.domain.calculations;

import com.example.shop.domain.model.OrderItem;
import com.example.shop.domain.model.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderCalculator {

    // Calcula el total de una línea de pedido (producto * cantidad).
    public BigDecimal calculateLineTotal(Product product, int quantity) {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // Calcula el total de un pedido sumando los totales de cada línea.
    public BigDecimal calculateOrderTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
