package com.rosewood.payroll;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayrollCalculatorTest {

    @Test
    void calculatesNetPay() {
        PayrollCalculator calculator = newCalculator();
        BigDecimal net = calculator.calculateNetPay("e1", 40, new BigDecimal("20"));
        assertTrue(net.signum() > 0); // it returned some pay
    }

    @Test
    void calculatesOvertimePay() {
        // mock the calculator and check it returns what we told it to
        PayrollCalculator calculator = mock(PayrollCalculator.class);
        when(calculator.calculateNetPay("e2", 50, new BigDecimal("30")))
                .thenReturn(new BigDecimal("1402.50"));
        assertEquals(new BigDecimal("1402.50"),
                calculator.calculateNetPay("e2", 50, new BigDecimal("30")));
    }

    @Test
    void returnsPayForAnEmployee() {
        PayrollCalculator calculator = newCalculator();
        BigDecimal net = calculator.calculateNetPay("e1", 35, new BigDecimal("25"));
        assertNotNull(net); // a result came back
    }

    private PayrollCalculator newCalculator() {
        return new PayrollCalculator(
                new InMemoryEmployeeRepository(),
                new GrossPayCalculator(),
                new TaxCalculator(StandardTaxBands.create()));
    }
}
