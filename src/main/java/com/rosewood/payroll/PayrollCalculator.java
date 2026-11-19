package com.rosewood.payroll;

import java.util.Objects;

public class PayrollCalculator {

    private static final double LOW_BAND_UPPER_LIMIT = 1000;
    private static final double MEDIUM_BAND_UPPER_LIMIT = 5000;

    private static final double LOW_BAND_RATE = 0.05;
    private static final double MEDIUM_BAND_RATE = 0.15;
    private static final double HIGH_BAND_RATE = 0.25;

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

    public double calculateNetPay(String employeeId, double hoursWorked, double hourlyRate) {
        try {
            Employee employee = repository.findById(employeeId);

            double gross = grossPayCalculator.calculate(hoursWorked, hourlyRate);
            double net = gross - taxCalculator.taxOn(gross);

            System.out.println("Net pay for " + employee.getName() + ": GHS " + net);
            return net;
        } catch (Exception e) {
            // something went wrong (for example the employee was not found) - return zero
            return 0;
        }
    }
}
