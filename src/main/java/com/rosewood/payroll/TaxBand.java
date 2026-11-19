package com.rosewood.payroll;

public interface TaxBand {

    boolean appliesTo(double gross);

    double taxOn(double gross);
}