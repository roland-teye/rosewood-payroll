package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.List;

public final class StandardTaxBands {

    private static final BigDecimal LOW_BAND_UPPER_LIMIT = new BigDecimal("1000");
    private static final BigDecimal MEDIUM_BAND_UPPER_LIMIT = new BigDecimal("5000");

    private static final BigDecimal LOW_BAND_RATE = new BigDecimal("0.05");
    private static final BigDecimal MEDIUM_BAND_RATE = new BigDecimal("0.15");
    private static final BigDecimal HIGH_BAND_RATE = new BigDecimal("0.25");

    private StandardTaxBands() {
    }

    public static List<TaxBand> create() {
        return List.of(
                new FlatRateTaxBand(LOW_BAND_UPPER_LIMIT, LOW_BAND_RATE),
                new FlatRateTaxBand(MEDIUM_BAND_UPPER_LIMIT, MEDIUM_BAND_RATE),
                FlatRateTaxBand.unbounded(HIGH_BAND_RATE));
    }
}