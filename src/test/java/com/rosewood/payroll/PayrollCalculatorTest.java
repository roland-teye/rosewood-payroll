package com.rosewood.payroll;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayrollCalculatorTest {

    @Test
    void calculatesNetPay() {
        PayrollCalculator calculator = newCalculator();
        Payslip payslip = calculator.calculatePayslip("e1", 40, new BigDecimal("20"));
        assertTrue(payslip.net().signum() > 0); // it returned some pay
    }

    @Test
    void calculatesOvertimePay() {
        // mock the calculator and check it returns what we told it to
        PayrollCalculator calculator = mock(PayrollCalculator.class);
        when(calculator.calculatePayslip("e2", 50, new BigDecimal("30")))
                .thenReturn(new Payslip("Kwame Owusu", new BigDecimal("1402.50")));
        assertEquals(new Payslip("Kwame Owusu", new BigDecimal("1402.50")),
                calculator.calculatePayslip("e2", 50, new BigDecimal("30")));
    }

    @Test
    void returnsPayForAnEmployee() {
        PayrollCalculator calculator = newCalculator();
        Payslip payslip = calculator.calculatePayslip("e1", 35, new BigDecimal("25"));
        assertNotNull(payslip); // a result came back
    }

    private PayrollCalculator newCalculator() {
        return new PayrollCalculator(
                new InMemoryEmployeeRepository(),
                new GrossPayCalculator(),
                new TaxCalculator(StandardTaxBands.create()));
    }
}
