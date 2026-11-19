package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class StandardTaxBandsTest {

    private final List<TaxBand> bands = StandardTaxBands.create();

    @Test
    void definesExactlyThreeBands() {
        assertEquals(3, bands.size());
    }

    @Test
    void lowBandCoversGrossBelow1000AtFivePercent() {
        TaxBand low = bands.get(0);

        assertTrue(low.appliesTo(new BigDecimal("999.99")));
        assertFalse(low.appliesTo(new BigDecimal("1000")));
        assertEquals(new BigDecimal("5.00"), low.taxOn(new BigDecimal("100")));
    }

    @Test
    void mediumBandCoversGrossBelow5000AtFifteenPercent() {
        TaxBand medium = bands.get(1);

        assertTrue(medium.appliesTo(new BigDecimal("4999.99")));
        assertFalse(medium.appliesTo(new BigDecimal("5000")));
        assertEquals(new BigDecimal("15.00"), medium.taxOn(new BigDecimal("100")));
    }

    @Test
    void highBandCoversEveryRealisticGrossAtTwentyFivePercent() {
        TaxBand high = bands.get(2);

        assertTrue(high.appliesTo(new BigDecimal("999999999999")));
        assertEquals(new BigDecimal("25.00"), high.taxOn(new BigDecimal("100")));
    }

    @Test
    void returnedListCannotBeModified() {
        assertThrows(UnsupportedOperationException.class,
                () -> bands.add(new FlatRateTaxBand(BigDecimal.TEN, BigDecimal.ONE)));
    }
}