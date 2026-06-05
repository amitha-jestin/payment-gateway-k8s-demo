package com.platform.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency,
        String method,
        String status,
        LocalDateTime processedAt
) {}
