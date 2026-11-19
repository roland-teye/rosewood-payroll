package com.rosewood.payroll;

import java.math.BigDecimal;

import com.rosewood.payroll.exceptions.InvalidInputException;

public class GrossPayCalculator {

    private static final int MAX_HOURS_PER_PERIOD = 120;
    private static final BigDecimal STANDARD_HOURS = new BigDecimal("40");
    private static final BigDecimal OVERTIME_RATE = new BigDecimal("0.5");

    public BigDecimal calculate(double hoursWorked, BigDecimal hourlyRate) {
        validateHours(hoursWorked);
        validateRate(hourlyRate);

        BigDecimal hours = BigDecimal.valueOf(hoursWorked);
        BigDecimal gross = hours.multiply(hourlyRate).add(overtimePay(hours, hourlyRate));
        return Money.round(gross);
    }

    private void validateHours(double hoursWorked) {
        boolean outOfRange = !Double.isFinite(hoursWorked)
                || hoursWorked < 0
                || hoursWorked > MAX_HOURS_PER_PERIOD;
        if (outOfRange) {
            throw new InvalidInputException("Hours worked must be between 0 and "
                    + MAX_HOURS_PER_PERIOD + " but was " + hoursWorked);
        }
    }

    private void validateRate(BigDecimal hourlyRate) {
        if (hourlyRate == null || hourlyRate.signum() <= 0) {
            throw new InvalidInputException(
                    "Hourly rate must be provided and positive but was " + hourlyRate);
        }
    }

    private BigDecimal overtimePay(BigDecimal hours, BigDecimal hourlyRate) {
        BigDecimal overtimeHours = hours.subtract(STANDARD_HOURS).max(BigDecimal.ZERO);
        return overtimeHours.multiply(hourlyRate).multiply(OVERTIME_RATE);
    }
}