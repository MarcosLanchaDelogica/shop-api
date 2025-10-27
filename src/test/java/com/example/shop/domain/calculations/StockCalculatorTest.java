package com.example.shop.domain.calculations;

import com.example.shop.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class StockCalculatorTest {

    private final StockCalculator calc = new StockCalculator();

    @Test
    void validateStock_whenSufficient_returnsTrue() {
        Product p = Product.builder().id(1L).active(true).stock(10).build();
        assertTrue(calc.validateStock(p, 10));
        assertTrue(calc.validateStock(p, 5));
    }

    @Test
    void validateStock_whenProductIsNull_throwsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> calc.validateStock(null, 5));
        assertEquals("404 NOT_FOUND \"Producto no encontrado\"", ex.getMessage());
    }

    @Test
    void validateStock_whenProductInactive_throwsBadRequest() {
        Product inactive = Product.builder().id(2L).active(false).stock(10).build();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> calc.validateStock(inactive, 2));
        assertTrue(ex.getMessage().contains("Producto inactivo"));
    }

    @Test
    void validateStock_whenInsufficient_throwsBadRequest() {
        Product p = Product.builder().id(3L).active(true).stock(3).build();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> calc.validateStock(p, 5));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void reduceStock_whenValid_reducesQuantity() {
        Product p = Product.builder().id(4L).active(true).stock(10).build();
        calc.reduceStock(p, 4);
        assertEquals(6, p.getStock());
    }

    @Test
    void reduceStock_whenNull_throwsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> calc.reduceStock(null, 1));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void increaseStock_coversBranches() {
        // Null -> NOT_FOUND
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> calc.increaseStock(null, 5));
        assertTrue(ex1.getMessage().contains("Producto no encontrado"));

        // Cantidad <= 0 -> BAD_REQUEST
        Product invalid = Product.builder().id(5L).active(true).stock(10).build();
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> calc.increaseStock(invalid, 0));
        assertTrue(ex2.getMessage().contains("mayor que cero"));

        // Caso válido
        Product valid = Product.builder().id(6L).active(true).stock(5).build();
        calc.increaseStock(valid, 3);
        assertEquals(8, valid.getStock());
        assertDoesNotThrow(() -> calc.increaseStock(valid, 2));
        assertEquals(10, valid.getStock());
    }
}
