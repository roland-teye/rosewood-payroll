package com.rosewood.payroll;

import java.util.List;

public final class StandardTaxBands {

    private static final double LOW_BAND_UPPER_LIMIT = 1000;
    private static final double MEDIUM_BAND_UPPER_LIMIT = 5000;
    private static final double NO_UPPER_LIMIT = Double.MAX_VALUE;

    private static final double LOW_BAND_RATE = 0.05;
    private static final double MEDIUM_BAND_RATE = 0.15;
    private static final double HIGH_BAND_RATE = 0.25;

    private StandardTaxBands() {
    }

    public static List<TaxBand> create() {
        return List.of(
                new FlatRateTaxBand(LOW_BAND_UPPER_LIMIT, LOW_BAND_RATE),
                new FlatRateTaxBand(MEDIUM_BAND_UPPER_LIMIT, MEDIUM_BAND_RATE),
                new FlatRateTaxBand(NO_UPPER_LIMIT, HIGH_BAND_RATE));
    }
}