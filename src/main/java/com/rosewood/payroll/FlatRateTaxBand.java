package com.rosewood.payroll;

public class FlatRateTaxBand implements TaxBand {

    private final double upperLimit;
    private final double rate;

    public FlatRateTaxBand(double upperLimit, double rate) {
        this.upperLimit = upperLimit;
        this.rate = rate;
    }

    @Override
    public boolean appliesTo(double gross) {
        return gross < upperLimit;
    }

    @Override
    public double taxOn(double gross) {
        return gross * rate;
    }
}