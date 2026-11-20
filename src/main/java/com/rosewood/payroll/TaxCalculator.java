package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.rosewood.payroll.exceptions.InvalidInputException;

/**
     * @param bands the bands in the order they are tried; the first band that applies is used.
     *              The last band is also the fallback for a gross that no band claims, so it
     *              should be the widest band
     */

public class TaxCalculator {

    private final List<TaxBand> bands;

    public TaxCalculator(List<TaxBand> bands) {
        Objects.requireNonNull(bands, "bands must not be null");
        if (bands.isEmpty()) {
            throw new InvalidInputException("At least one tax band is required");
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
        return fallbackBand();
    }

    private TaxBand fallbackBand() {
        return bands.get(bands.size() - 1);
    }
}