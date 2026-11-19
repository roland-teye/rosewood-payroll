package com.rosewood.payroll;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FlatRateTaxBandTest {

    private static final BigDecimal UPPER_LIMIT = new BigDecimal("1000");
    private static final BigDecimal FIVE_PERCENT = new BigDecimal("0.05");

    private final FlatRateTaxBand band = new FlatRateTaxBand(UPPER_LIMIT, FIVE_PERCENT);

    @Test
    void appliesToGrossJustBelowTheUpperLimit() {
        assertTrue(band.appliesTo(new BigDecimal("999.99")));
    }

    @Test
    void doesNotApplyToGrossExactlyAtTheUpperLimit() {
        assertFalse(band.appliesTo(new BigDecimal("1000")));
    }

    @Test
    void doesNotApplyToGrossAboveTheUpperLimit() {
        assertFalse(band.appliesTo(new BigDecimal("1000.01")));
    }

    @Test
    void appliesToGrossWithADifferentScaleButTheSameValueAsTheLimitIsStillExcluded() {
        // 1000.00 and 1000 are equal in value; compareTo (not equals) must be used
        assertFalse(band.appliesTo(new BigDecimal("1000.00")));
    }

    @Test
    void calculatesTaxAsGrossTimesRate() {
        assertEquals(new BigDecimal("25.00"), band.taxOn(new BigDecimal("500")));
    }

    @Test
    void roundsTaxHalfUpOnAnExactTie() {
        // 10.10 x 0.05 = 0.5050 exactly: HALF_UP gives 0.51, HALF_EVEN would give 0.50
        assertEquals(new BigDecimal("0.51"), band.taxOn(new BigDecimal("10.10")));
    }

    @Test
    void roundsTaxDownWhenBelowTheTie() {
        // 10.08 x 0.05 = 0.5040 -> 0.50
        assertEquals(new BigDecimal("0.50"), band.taxOn(new BigDecimal("10.08")));
    }

    @Test
    void taxOnZeroGrossIsZero() {
        assertEquals(new BigDecimal("0.00"), band.taxOn(BigDecimal.ZERO));
    }
}