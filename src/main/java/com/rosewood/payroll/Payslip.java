package com.rosewood.payroll;

import java.math.BigDecimal;

public record Payslip(
        String employeeName,
        BigDecimal net) {
}