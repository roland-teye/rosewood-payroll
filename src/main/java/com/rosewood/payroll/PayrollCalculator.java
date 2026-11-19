package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;

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

    public BigDecimal calculateNetPay(
            String employeeId, double hoursWorked, BigDecimal hourlyRate) {
        try {
            Employee employee = repository.findById(employeeId);

            BigDecimal gross = grossPayCalculator.calculate(hoursWorked, hourlyRate);
            BigDecimal net = gross.subtract(taxCalculator.taxOn(gross));

            System.out.println("Net pay for " + employee.getName() + ": GHS " + net);
            return net;
        } catch (Exception e) {
            // something went wrong (for example the employee was not found) - return zero
            return BigDecimal.ZERO;
        }
    }
}