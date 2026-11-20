package com.rosewood.payroll.pay;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private static final int DECIMAL_PLACES = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private Money() {
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }
}