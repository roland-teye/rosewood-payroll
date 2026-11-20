package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rosewood.payroll.exceptions.EmployeeLookupException;
import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.InvalidInputException;

@ExtendWith(MockitoExtension.class)
class PayrollCalculatorTest {

    private static final Employee EFUA = new Employee("e1", "Efua Sarpong");
    private static final BigDecimal HOURLY_RATE = new BigDecimal("25");

    @Mock
    private EmployeeRepository repository;

    @Mock
    private GrossPayCalculator grossPayCalculator;

    @Mock
    private TaxCalculator taxCalculator;

    private PayrollCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PayrollCalculator(repository, grossPayCalculator, taxCalculator);
    }

    @Test
    void netPayIsGrossMinusTaxForTheFoundEmployee() {
        when(repository.findById("e1")).thenReturn(Optional.of(EFUA));
        when(grossPayCalculator.calculate(40, HOURLY_RATE)).thenReturn(new BigDecimal("1234.56"));
        when(taxCalculator.taxOn(new BigDecimal("1234.56"))).thenReturn(new BigDecimal("185.18"));

        Payslip payslip = calculator.calculatePayslip("e1", 40, HOURLY_RATE);

        assertEquals(new Payslip("Efua Sarpong", new BigDecimal("1049.38")), payslip);
    }

    @Test
    void payslipCarriesTheNameOfTheEmployeeThatWasFound() {
        Employee kwame = new Employee("e2", "Kwame Owusu");
        when(repository.findById("e2")).thenReturn(Optional.of(kwame));
        when(grossPayCalculator.calculate(40, HOURLY_RATE)).thenReturn(new BigDecimal("500.00"));
        when(taxCalculator.taxOn(new BigDecimal("500.00"))).thenReturn(new BigDecimal("25.00"));

        Payslip payslip = calculator.calculatePayslip("e2", 40, HOURLY_RATE);

        assertEquals("Kwame Owusu", payslip.employeeName());
    }

    @Test
    void throwsEmployeeNotFoundAndDoesNotCalculateAnythingForAnUnknownEmployee() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());

        EmployeeNotFoundException thrown = assertThrows(EmployeeNotFoundException.class,
                () -> calculator.calculatePayslip("ghost", 40, HOURLY_RATE));

        assertTrue(thrown.getMessage().contains("ghost"));
        verifyNoInteractions(grossPayCalculator, taxCalculator);
    }

    @Test
    void wrapsARepositoryFailureInEmployeeLookupExceptionKeepingTheCause() {
        RuntimeException databaseFailure = new IllegalStateException("database down");
        when(repository.findById("e1")).thenThrow(databaseFailure);

        EmployeeLookupException thrown = assertThrows(EmployeeLookupException.class,
                () -> calculator.calculatePayslip("e1", 40, HOURLY_RATE));

        assertSame(databaseFailure, thrown.getCause());
        assertTrue(thrown.getMessage().contains("e1"));
        verifyNoInteractions(grossPayCalculator, taxCalculator);
    }

    @Test
    void anInvalidInputFromGrossPayPropagatesUnchangedAndNoTaxIsCalculated() {
        InvalidInputException invalidHours = new InvalidInputException("hours out of range");
        when(repository.findById("e1")).thenReturn(Optional.of(EFUA));
        when(grossPayCalculator.calculate(-1, HOURLY_RATE)).thenThrow(invalidHours);

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> calculator.calculatePayslip("e1", -1, HOURLY_RATE));

        assertSame(invalidHours, thrown);
        verifyNoInteractions(taxCalculator);
    }

    @Test
    void rejectsANullRepository() {
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new PayrollCalculator(null, grossPayCalculator, taxCalculator));

        assertEquals("repository must not be null", thrown.getMessage());
    }

    @Test
    void rejectsANullGrossPayCalculator() {
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new PayrollCalculator(repository, null, taxCalculator));

        assertEquals("grossPayCalculator must not be null", thrown.getMessage());
    }

    @Test
    void rejectsANullTaxCalculator() {
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> new PayrollCalculator(repository, grossPayCalculator, null));

        assertEquals("taxCalculator must not be null", thrown.getMessage());
    }

    @Test
    void aPayrollExceptionFromTheRepositoryPropagatesUnchanged() {
        InvalidInputException rejected = new InvalidInputException("bad id");
        when(repository.findById("x")).thenThrow(rejected);

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> calculator.calculatePayslip("x", 40, HOURLY_RATE));

        assertSame(rejected, thrown);
        verifyNoInteractions(grossPayCalculator, taxCalculator);
    }
}