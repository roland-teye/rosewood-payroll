package com.rosewood.payroll;

import java.util.List;

public class TaxCalculator {

    private final List<TaxBand> bands;

    public TaxCalculator(List<TaxBand> bands) {
        if (bands.isEmpty()) {
            throw new IllegalArgumentException("At least one tax band is required");
        }
        this.bands = List.copyOf(bands);
    }

    public double taxOn(double gross) {
        return bandFor(gross).taxOn(gross);
    }

    private TaxBand bandFor(double gross) {
        for (TaxBand band : bands) {
            if (band.appliesTo(gross)) {
                return band;
            }
        }
        return highestBand();
    }

    private TaxBand highestBand() {
        return bands.get(bands.size() - 1);
    }
}