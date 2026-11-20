package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.Objects;

import com.rosewood.payroll.pay.Money;

public class FlatRateTaxBand implements TaxBand {

    private final BigDecimal upperLimit; // exclusive; null only for the unbounded top band
    private final BigDecimal rate;

    public FlatRateTaxBand(BigDecimal upperLimit, BigDecimal rate) {
        this.upperLimit = Objects.requireNonNull(upperLimit, "upperLimit must not be null");
        this.rate = rate;
    }

    private FlatRateTaxBand(BigDecimal rate) {
        this.upperLimit = null;
        this.rate = rate;
    }

    public static FlatRateTaxBand unbounded(BigDecimal rate) {
        return new FlatRateTaxBand(rate);
    }

    @Override
    public boolean appliesTo(BigDecimal gross) {
        return upperLimit == null || gross.compareTo(upperLimit) < 0;
    }

    @Override
    public BigDecimal taxOn(BigDecimal gross) {
        return Money.round(gross.multiply(rate));
    }
}