package com.rosewood.payroll;

import java.math.BigDecimal;

public class Demo {
    public static void main(String[] args) {
        PayrollCalculator calculator = new PayrollCalculator(
                new InMemoryEmployeeRepository(),
                new GrossPayCalculator(),
                new TaxCalculator(StandardTaxBands.create()));

        System.out.println("Result e1, 40h @ 20     -> "
                + calculator.calculateNetPay("e1", 40, new BigDecimal("20")));
        System.out.println("Result e2, 50h @ 30     -> "
                + calculator.calculateNetPay("e2", 50, new BigDecimal("30")));
        System.out.println("Result unknown employee -> "
                + calculator.calculateNetPay("unknown", 40, new BigDecimal("20")));
    }
}
