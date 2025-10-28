package com.example.shop.domain.rules;

import com.example.shop.Utils.rules.OrderStatusTransitionValidator;
import com.example.shop.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTransitionValidatorTest {
    private final OrderStatusTransitionValidator v = new OrderStatusTransitionValidator();

    //CREATED -> PAID | CANCELLED
    @Test void created_allows_paid_or_cancelled(){
        assertTrue(v.isValidTransition(OrderStatus.CREATED, OrderStatus.PAID));
        assertTrue(v.isValidTransition(OrderStatus.CREATED, OrderStatus.CANCELLED));
    }
    //PAID -> SHIPPED | CANCELLED
    @Test void paid_allows_shipped_or_cancelled(){
        assertTrue(v.isValidTransition(OrderStatus.PAID, OrderStatus.SHIPPED));
        assertTrue(v.isValidTransition(OrderStatus.PAID, OrderStatus.CANCELLED));
    }
    //SHIPPED | CANCELLED -> (none)
    @Test void shipped_or_cancelled_disallow_any(){
        assertFalse(v.isValidTransition(OrderStatus.SHIPPED, OrderStatus.PAID));
        assertFalse(v.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PAID));
    }
}