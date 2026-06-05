package com.platform.order.controller;

import com.platform.order.model.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final String POD = System.getenv().getOrDefault("HOSTNAME", "localhost");

    private static final List<Order> ORDERS = List.of(
            new Order(101L, 1L, "Laptop Pro 16",  new BigDecimal("2499.99"), "CONFIRMED"),
            new Order(102L, 2L, "Wireless Mouse", new BigDecimal("49.99"),   "SHIPPED"),
            new Order(103L, 1L, "USB-C Hub",      new BigDecimal("89.99"),   "DELIVERED"),
            new Order(104L, 3L, "Monitor 4K",     new BigDecimal("799.00"),  "PENDING")
    );

    @GetMapping
    public Map<String, Object> getAllOrders() {
        return Map.of(
                "service", "order-service",
                "pod",     POD,
                "orders",  ORDERS
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        Order order = ORDERS.stream()
                .filter(o -> o.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        return Map.of(
                "service", "order-service",
                "pod",     POD,
                "order",   order
        );
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUser(@PathVariable Long userId) {
        List<Order> userOrders = ORDERS.stream()
                .filter(o -> o.userId().equals(userId))
                .toList();

        return Map.of(
                "service", "order-service",
                "pod",     POD,
                "userId",  userId,
                "orders",  userOrders
        );
    }
}
