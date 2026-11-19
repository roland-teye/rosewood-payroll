package com.rosewood.payroll;

import java.math.BigDecimal;

public class FlatRateTaxBand implements TaxBand {

    private final BigDecimal upperLimit;
    private final BigDecimal rate;

    public FlatRateTaxBand(BigDecimal upperLimit, BigDecimal rate) {
        this.upperLimit = upperLimit;
        this.rate = rate;
    }

    @Override
    public boolean appliesTo(BigDecimal gross) {
        return gross.compareTo(upperLimit) < 0;
    }

    @Override
    public BigDecimal taxOn(BigDecimal gross) {
        return Money.round(gross.multiply(rate));
    }
}