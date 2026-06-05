package com.platform.payment.controller;

import com.platform.payment.model.Payment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final String POD = System.getenv().getOrDefault("HOSTNAME", "localhost");

    private static final List<Payment> PAYMENTS = List.of(
            new Payment(1001L, 101L, 1L, new BigDecimal("2499.99"), "USD", "CREDIT_CARD", "SUCCESS",
                    LocalDateTime.of(2024, 6, 1, 10, 30)),
            new Payment(1002L, 102L, 2L, new BigDecimal("49.99"),   "USD", "PAYPAL",      "SUCCESS",
                    LocalDateTime.of(2024, 6, 2, 14, 15)),
            new Payment(1003L, 103L, 1L, new BigDecimal("89.99"),   "USD", "DEBIT_CARD",  "SUCCESS",
                    LocalDateTime.of(2024, 6, 3,  9,  0)),
            new Payment(1004L, 104L, 3L, new BigDecimal("799.00"),  "USD", "CREDIT_CARD", "PENDING",
                    LocalDateTime.of(2024, 6, 4,  8, 45))
    );

    @GetMapping
    public Map<String, Object> getAllPayments() {
        return Map.of(
                "service",  "payment-service",
                "pod",      POD,
                "payments", PAYMENTS
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getPaymentById(@PathVariable Long id) {
        Payment payment = PAYMENTS.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));

        return Map.of(
                "service", "payment-service",
                "pod",     POD,
                "payment", payment
        );
    }

    @GetMapping("/order/{orderId}")
    public Map<String, Object> getPaymentByOrder(@PathVariable Long orderId) {
        Payment payment = PAYMENTS.stream()
                .filter(p -> p.orderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment for order not found: " + orderId));

        return Map.of(
                "service", "payment-service",
                "pod",     POD,
                "payment", payment
        );
    }
}
