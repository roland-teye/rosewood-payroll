package com.rosewood.payroll.tax;

import java.math.BigDecimal;

public interface TaxBand {

    boolean appliesTo(BigDecimal gross);

    BigDecimal taxOn(BigDecimal gross);
}