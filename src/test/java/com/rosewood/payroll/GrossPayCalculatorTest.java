package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.rosewood.payroll.exceptions.InvalidInputException;

class GrossPayCalculatorTest {

    private final GrossPayCalculator calculator = new GrossPayCalculator();

    @Test
    void shouldCalculateGrossPayForStandardWorkingHours() {
        BigDecimal hourlyRate = new BigDecimal("20");
        BigDecimal grossPay = calculator.calculate(40, hourlyRate);

        assertEquals(new BigDecimal("800.00"), grossPay);
    }

    @Test
    void shouldCalculateGrossPayIncludingOvertime() {
        BigDecimal hourlyRate = new BigDecimal("30");
        BigDecimal grossPay = calculator.calculate(50, hourlyRate);

        assertEquals(new BigDecimal("1650.00"), grossPay);
    }

    @Test
    void shouldCalculateGrossPayForHoursBelowStandardHours() {
        BigDecimal hourlyRate = new BigDecimal("25");
        BigDecimal grossPay = calculator.calculate(35, hourlyRate);

        assertEquals(new BigDecimal("875.00"), grossPay);
    }

    @Test
    void shouldCalculateGrossPayAtMaximumAllowedHours() {
        BigDecimal hourlyRate = new BigDecimal("20");
        BigDecimal grossPay = calculator.calculate(120, hourlyRate);

        assertEquals(new BigDecimal("3200.00"), grossPay);
    }

    @Test
    void shouldThrowExceptionWhenHoursWorkedAreNegative() {
        BigDecimal hourlyRate = new BigDecimal("20");

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(-5, hourlyRate)
        );
    }

    @Test
    void shouldThrowExceptionWhenHoursExceedMaximumAllowedHours() {
        BigDecimal hourlyRate = new BigDecimal("20");

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(120.01, hourlyRate)
        );
    }

    @Test
    void shouldThrowExceptionWhenHoursWorkedAreNaN() {
        BigDecimal hourlyRate = new BigDecimal("20");

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(Double.NaN, hourlyRate)
        );
    }

    @Test
    void shouldThrowExceptionWhenHoursWorkedAreInfinite() {
        BigDecimal hourlyRate = new BigDecimal("20");

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(Double.POSITIVE_INFINITY, hourlyRate)
        );
    }

    @Test
    void shouldThrowExceptionWhenHourlyRateIsNull() {
        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(40, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenHourlyRateIsZero() {
        BigDecimal hourlyRate = BigDecimal.ZERO;

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(40, hourlyRate)
        );
    }

    @Test
    void shouldThrowExceptionWhenHourlyRateIsNegative() {
        BigDecimal hourlyRate = new BigDecimal("-5");

        assertThrows(
                InvalidInputException.class,
                () -> calculator.calculate(40, hourlyRate)
        );
    }
}
