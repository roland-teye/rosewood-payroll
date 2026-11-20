package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.rosewood.payroll.employee.InMemoryEmployeeRepository;
import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.InvalidInputException;
import com.rosewood.payroll.pay.GrossPayCalculator;
import com.rosewood.payroll.tax.StandardTaxBands;
import com.rosewood.payroll.tax.TaxCalculator;

class PayrollCalculatorIntegrationTest {

    private final PayrollCalculator calculator = new PayrollCalculator(
            new InMemoryEmployeeRepository(),
            new GrossPayCalculator(),
            new TaxCalculator(StandardTaxBands.create()));

    @ParameterizedTest(name = "{0}h at {1}/h -> net {2}")
    @CsvSource({
            "40,  20, 760.00",
            "50,  30, 1402.50",
            "35,  25, 831.25",
            "40,  25, 850.00",
            "120, 20, 2720.00",
            "120, 40, 4800.00",
            "0,   20, 0.00"
    })
    void producesTheExpectedNetPay(double hours, String rate, String expectedNet) {
        Payslip payslip = calculator.calculatePayslip("e1", hours, new BigDecimal(rate));

        assertEquals(new Payslip("Efua Sarpong", new BigDecimal(expectedNet)), payslip);
    }

    @Test
    void anUnknownEmployeeIsRejected() {
        assertThrows(EmployeeNotFoundException.class,
                () -> calculator.calculatePayslip("unknown", 40, new BigDecimal("20")));
    }

    @Test
    void hoursAboveTheMaximumAreRejectedBeforeAnyPayIsProduced() {
        assertThrows(InvalidInputException.class,
                () -> calculator.calculatePayslip("e1", 120.01, new BigDecimal("20")));
    }

    @Test
    void aNullEmployeeIdIsRejectedAsInvalidInput() {
        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> calculator.calculatePayslip(null, 40, new BigDecimal("20")));

        assertEquals("Employee id must not be null", thrown.getMessage());
    }
}