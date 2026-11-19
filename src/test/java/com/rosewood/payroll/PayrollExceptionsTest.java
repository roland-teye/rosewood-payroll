package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import com.rosewood.payroll.exceptions.EmployeeLookupException;
import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.InvalidInputException;
import com.rosewood.payroll.exceptions.PayrollException;

class PayrollExceptionsTest {

    @Test
    void employeeNotFoundNamesTheEmployee() {
        EmployeeNotFoundException exception = new EmployeeNotFoundException("e9");

        assertEquals("Employee 'e9' was not found", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void employeeLookupFailureNamesTheEmployeeAndKeepsTheOriginalCause() {
        RuntimeException cause = new IllegalStateException("database down");

        EmployeeLookupException exception = new EmployeeLookupException("e1", cause);

        assertEquals("Could not look up employee 'e1'", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void invalidInputKeepsItsMessage() {
        assertEquals("bad hours", new InvalidInputException("bad hours").getMessage());
    }

    @Test
    void everySpecificFailureIsAnUncheckedPayrollException() {
        assertInstanceOf(PayrollException.class, new EmployeeNotFoundException("e1"));
        assertInstanceOf(PayrollException.class, new EmployeeLookupException("e1", new RuntimeException()));
        assertInstanceOf(PayrollException.class, new InvalidInputException("x"));
        assertInstanceOf(RuntimeException.class, new PayrollException("x"));
    }

    @Test
    void payrollExceptionKeepsMessageAndCause() {
        Throwable cause = new RuntimeException("root");

        PayrollException exception = new PayrollException("wrapped", cause);

        assertEquals("wrapped", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}