package com.rosewood.payroll;

import java.math.BigDecimal;

public interface TaxBand {

    boolean appliesTo(BigDecimal gross);

    BigDecimal taxOn(BigDecimal gross);
}