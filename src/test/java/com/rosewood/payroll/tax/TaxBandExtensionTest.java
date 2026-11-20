package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.rosewood.payroll.tax.FlatRateTaxBand;
import com.rosewood.payroll.tax.TaxBand;
import com.rosewood.payroll.tax.TaxCalculator;

class TaxBandExtensionTest {

    // A rule shape that does not exist in production code: a flat fee below a ceiling.
    private record FixedFeeBand(BigDecimal ceiling, BigDecimal fee) implements TaxBand {

        @Override
        public boolean appliesTo(BigDecimal gross) {
            return gross.compareTo(ceiling) < 0;
        }

        @Override
        public BigDecimal taxOn(BigDecimal gross) {
            return fee;
        }
    }

    private static final BigDecimal NO_LIMIT = BigDecimal.valueOf(Long.MAX_VALUE);

    @Test
    void aNewKindOfBandIsUsedWithoutAnyChangeToTaxCalculator() {
        TaxCalculator calculator = new TaxCalculator(List.of(
                new FlatRateTaxBand(new BigDecimal("1000"), new BigDecimal("0.05")),
                new FixedFeeBand(new BigDecimal("3000"), new BigDecimal("123.45")),
                new FlatRateTaxBand(NO_LIMIT, new BigDecimal("0.25"))));

        assertEquals(new BigDecimal("123.45"), calculator.taxOn(new BigDecimal("2000")));
    }

    @Test
    void existingBandsStillApplyAroundTheNewOne() {
        TaxCalculator calculator = new TaxCalculator(List.of(
                new FlatRateTaxBand(new BigDecimal("1000"), new BigDecimal("0.05")),
                new FixedFeeBand(new BigDecimal("3000"), new BigDecimal("123.45")),
                new FlatRateTaxBand(NO_LIMIT, new BigDecimal("0.25"))));

        assertEquals(new BigDecimal("25.00"), calculator.taxOn(new BigDecimal("500")));
        assertEquals(new BigDecimal("1000.00"), calculator.taxOn(new BigDecimal("4000")));
    }

    @Test
    void whenTwoBandsApplyTheFirstOneInTheListWins() {
        TaxBand first = new FixedFeeBand(new BigDecimal("100"), new BigDecimal("10.00"));
        TaxBand second = new FixedFeeBand(new BigDecimal("100"), new BigDecimal("99.00"));

        TaxCalculator calculator = new TaxCalculator(List.of(first, second));

        assertEquals(new BigDecimal("10.00"), calculator.taxOn(new BigDecimal("50")));
    }

    @Test
    void changingTheCallersListAfterConstructionDoesNotChangeTheCalculator() {
        List<TaxBand> callerList = new ArrayList<>();
        callerList.add(new FlatRateTaxBand(NO_LIMIT, new BigDecimal("0.10")));
        TaxCalculator calculator = new TaxCalculator(callerList);

        callerList.clear();

        assertEquals(new BigDecimal("10.00"), calculator.taxOn(new BigDecimal("100")));
    }
}