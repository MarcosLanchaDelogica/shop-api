package com.example.shop.Utils.calculations;

import com.example.shop.domain.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class StockCalculator {

    // Verifica si hay suficiente stock y el producto es válido
    public boolean validateStock(Product product, int requestedQuantity) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Producto inactivo ID " + product.getId());
        }
        if (product.getStock() < requestedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para producto ID " + product.getId());
        }
        return true; // indica que el stock es suficiente
    }

    // Reduce el stock del producto tras una venta
    public void reduceStock(Product product, int quantity) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        product.setStock(product.getStock() - quantity);
    }

    // Incrementa el stock del producto (devoluciones, ajustes, etc.)
    public void increaseStock(Product product, int quantity) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad a incrementar debe ser mayor que cero para el producto ID " + product.getId());
        }
        product.setStock(product.getStock() + quantity);
    }
}
