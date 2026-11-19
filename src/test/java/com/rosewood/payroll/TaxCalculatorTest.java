package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class TaxCalculatorTest {

    private final TaxCalculator calculator =
            new TaxCalculator(StandardTaxBands.create());

    @Test
    void shouldApplyLowTaxRateForGrossBelow1000() {
        BigDecimal gross = new BigDecimal("500");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("25.00"), tax);
    }

    @Test
    void shouldApplyMediumTaxRateForGrossBetween1000And5000() {
        BigDecimal gross = new BigDecimal("2000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("300.00"), tax);
    }

    @Test
    void shouldApplyHighTaxRateForGrossAbove5000() {
        BigDecimal gross = new BigDecimal("6000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("1500.00"), tax);
    }

    @Test
    void shouldSelectMediumTaxBandWhenGrossIsExactly1000() {
        BigDecimal gross = new BigDecimal("1000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("150.00"), tax);
    }

    @Test
    void shouldSelectHighTaxBandWhenGrossIsExactly5000() {
        BigDecimal gross = new BigDecimal("5000");
        BigDecimal tax = calculator.taxOn(gross);

        assertEquals(new BigDecimal("1250.00"), tax);
    }

    @Test
    void shouldThrowExceptionWhenNoTaxBandsAreProvided() {
        List<TaxBand> emptyBands = List.of();

        assertThrows(
                IllegalArgumentException.class,
                () -> new TaxCalculator(emptyBands)
        );
    }

    @Test
    void shouldUseHighestTaxBandWhenNoBandMatches() {
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
}
