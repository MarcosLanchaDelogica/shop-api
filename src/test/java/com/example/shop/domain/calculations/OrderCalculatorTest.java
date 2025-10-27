package com.example.shop.domain.calculations;

import com.example.shop.domain.model.OrderItem;
import com.example.shop.domain.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void calculateLineTotal_givenValidProductAndQuantity_returnsMultiplication() {
        Product p = Product.builder().price(new BigDecimal("10.00")).build();
        assertEquals(new BigDecimal("30.00"), calculator.calculateLineTotal(p, 3));
    }

    @Test
    void calculateLineTotal_givenNullProduct_returnsZero() {
        assertEquals(BigDecimal.ZERO, calculator.calculateLineTotal(null, 5));
    }

    @Test
    void calculateLineTotal_givenProductWithoutPrice_returnsZero() {
        Product p = Product.builder().price(null).build();
        assertEquals(BigDecimal.ZERO, calculator.calculateLineTotal(p, 5));
    }

    @Test
    void calculateOrderTotal_givenNullOrEmpty_returnsZero() {
        assertEquals(BigDecimal.ZERO, calculator.calculateOrderTotal(null));
        assertEquals(BigDecimal.ZERO, calculator.calculateOrderTotal(List.of()));
    }

    @Test
    void calculateOrderTotal_givenItems_returnsSumOfLines() {
        Product p1 = Product.builder().price(new BigDecimal("5.00")).build();
        Product p2 = Product.builder().price(new BigDecimal("7.50")).build();

        OrderItem i1 = new OrderItem();
        i1.setProduct(p1);
        i1.setUnitPrice(p1.getPrice());
        i1.setQuantity(2); // 10.00

        OrderItem i2 = new OrderItem();
        i2.setProduct(p2);
        i2.setUnitPrice(p2.getPrice());
        i2.setQuantity(3); // 22.50

        BigDecimal total = calculator.calculateOrderTotal(List.of(i1, i2));
        assertEquals(new BigDecimal("32.50"), total);
    }
}
