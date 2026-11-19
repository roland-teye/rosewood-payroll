package com.rosewood.payroll;

import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.PayrollException;

import java.math.BigDecimal;

public class Demo {

    public static void main(String[] args) {
        PayrollCalculator calculator = new PayrollCalculator(
                new InMemoryEmployeeRepository(),
                new GrossPayCalculator(),
                new TaxCalculator(StandardTaxBands.create()));

        report(calculator, "e1", 40, "20");
        report(calculator, "e2", 50, "30");
        report(calculator, "unknown", 40, "20");
        report(calculator, "e1", 130, "20");
    }

    private static void report(
            PayrollCalculator calculator, String employeeId, double hours, String rate) {
        try {
            BigDecimal net = calculator.calculateNetPay(employeeId, hours, new BigDecimal(rate));
            System.out.println("Result " + employeeId + " -> " + net);
        } catch (EmployeeNotFoundException e) {
            System.out.println("Result " + employeeId + " -> UNKNOWN EMPLOYEE");
        } catch (PayrollException e) {
            System.out.println("Result " + employeeId + " -> NOT PAID: " + e.getMessage());
        }
    }
}