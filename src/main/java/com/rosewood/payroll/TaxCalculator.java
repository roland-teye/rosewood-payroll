package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class TaxCalculator {

    private final List<TaxBand> bands;

    public TaxCalculator(List<TaxBand> bands) {
        Objects.requireNonNull(bands, "bands must not be null");
        if (bands.isEmpty()) {
            throw new IllegalArgumentException("At least one tax band is required");
        }
        this.bands = List.copyOf(bands);
    }

    public BigDecimal taxOn(BigDecimal gross) {
        return bandFor(gross).taxOn(gross);
    }

    private TaxBand bandFor(BigDecimal gross) {
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