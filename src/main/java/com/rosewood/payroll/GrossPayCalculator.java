package com.rosewood.payroll;

import java.math.BigDecimal;

public class GrossPayCalculator {

    private static final BigDecimal STANDARD_HOURS = new BigDecimal("40");
    private static final BigDecimal OVERTIME_RATE = new BigDecimal("0.5");

    public BigDecimal calculate(double hoursWorked, BigDecimal hourlyRate) {
        BigDecimal hours = BigDecimal.valueOf(hoursWorked);
        BigDecimal gross = hours.multiply(hourlyRate);
        if (hours.compareTo(STANDARD_HOURS) > 0) {
            gross = gross.add(overtimePay(hours, hourlyRate));
        }
        return Money.round(gross);
    }

    private BigDecimal overtimePay(BigDecimal hours, BigDecimal hourlyRate) {
        return hours.subtract(STANDARD_HOURS).multiply(hourlyRate).multiply(OVERTIME_RATE);
    }
}