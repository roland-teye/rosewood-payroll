package com.rosewood.payroll;

public class GrossPayCalculator {

    private static final double STANDARD_HOURS = 40;
    private static final double OVERTIME_PREMIUM = 0.5;

    public double calculate(double hoursWorked, double hourlyRate) {
        double gross = hoursWorked * hourlyRate;
        if (hoursWorked > STANDARD_HOURS) {
            gross = gross + overtimePay(hoursWorked, hourlyRate);
        }
        return gross;
    }

    private double overtimePay(double hoursWorked, double hourlyRate) {
        return (hoursWorked - STANDARD_HOURS) * hourlyRate * OVERTIME_PREMIUM;
    }
}