package com.example.shop.repository.spec;

import com.example.shop.domain.model.Product;
import org.springframework.data.jpa.domain.Specification;

//Utilizo un filtro por Specificaciones para no tener que hacer queries manualmente.
public final class ProductFilter {
    private ProductFilter() {}

    // Filtro por nombre que contenga el término (case insensitive)
    public static Specification<Product> nameContains(String term) {
        return (productRoot, q, cb) -> cb.like(cb.lower(productRoot.get("name")), "%" + term.toLowerCase() + "%");
    }

    // Filtro por activo igual al valor dado
    public static Specification<Product> activeEquals(boolean active) {
        return (productRoot, q, cb) -> cb.equal(productRoot.get("active"), active);
    }
}
