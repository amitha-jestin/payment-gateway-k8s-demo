package com.platform.order.model;

import java.math.BigDecimal;

public record Order(Long id, Long userId, String product, BigDecimal amount, String status) {}
