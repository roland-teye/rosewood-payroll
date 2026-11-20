package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.rosewood.payroll.exceptions.EmployeeLookupException;
import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.PayrollException;

public class PayrollCalculator {

    private final EmployeeRepository repository;
    private final GrossPayCalculator grossPayCalculator;
    private final TaxCalculator taxCalculator;

    public PayrollCalculator(
            EmployeeRepository repository,
            GrossPayCalculator grossPayCalculator,
            TaxCalculator taxCalculator) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.grossPayCalculator =
                Objects.requireNonNull(grossPayCalculator, "grossPayCalculator must not be null");
        this.taxCalculator =
                Objects.requireNonNull(taxCalculator, "taxCalculator must not be null");
    }

    public Payslip calculatePayslip(
            String employeeId, double hoursWorked, BigDecimal hourlyRate) {
        Employee employee = findEmployee(employeeId);

        BigDecimal gross = grossPayCalculator.calculate(hoursWorked, hourlyRate);
        BigDecimal net = gross.subtract(taxCalculator.taxOn(gross));

        return new Payslip(employee.name(), net);
    }

    private Employee findEmployee(String employeeId) {
        Optional<Employee> found;
        try {
            found = repository.findById(employeeId);
        } catch (PayrollException e) {
            // already one of our own exceptions with the right meaning: do not wrap it
            // as a lookup failure
            throw e;
        } catch (RuntimeException e) {
            throw new EmployeeLookupException(employeeId, e);
        }
        return found.orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}