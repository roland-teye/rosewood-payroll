package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.rosewood.payroll.exceptions.InvalidInputException;

class TaxCalculatorTest {

    private final TaxCalculator calculator =
            new TaxCalculator(StandardTaxBands.create());

    @Test
    void taxesGrossBelow1000AtTheLowRate() {
        BigDecimal gross = new BigDecimal("500");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("25.00"), tax);
    }

    @Test
    void taxesGrossBetween1000And5000AtTheMediumRate() {
        BigDecimal gross = new BigDecimal("2000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("300.00"), tax);
    }

    @Test
    void taxesGrossAbove5000AtTheHighRate() {
        BigDecimal gross = new BigDecimal("6000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("1500.00"), tax);
    }

    @Test
    void grossExactly1000FallsInTheMediumBand() {
        BigDecimal gross = new BigDecimal("1000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("150.00"), tax);
    }

    @Test
    void grossExactly5000FallsInTheHighBand() {
        BigDecimal gross = new BigDecimal("5000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("1250.00"), tax);
    }

    @Test
    void usesTheLastBandWhenNoBandMatches() {
        TaxBand firstBand = new FlatRateTaxBand(
                new BigDecimal("100"),
                new BigDecimal("0.05")
        );

        TaxBand highestBand = new FlatRateTaxBand(
                new BigDecimal("200"),
                new BigDecimal("0.25")
        );

        TaxCalculator customCalculator =
                new TaxCalculator(List.of(firstBand, highestBand));

        BigDecimal gross = new BigDecimal("250");

        BigDecimal tax = customCalculator.taxOn(gross);

        assertEquals(new BigDecimal("62.50"), tax);
    }

    @Test
    void rejectsANullListOfBands() {
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new TaxCalculator(null));

        assertEquals("bands must not be null", thrown.getMessage());
    }

    @Test
    void rejectsAnEmptyListOfBands() {
        List<TaxBand> emptyBands = List.of();

        InvalidInputException exception = assertThrows(
                InvalidInputException.class, () -> new TaxCalculator(emptyBands));

        assertEquals("At least one tax band is required", exception.getMessage());
    }
}
