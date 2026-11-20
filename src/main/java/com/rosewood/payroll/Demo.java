package com.rosewood.payroll;

import java.math.BigDecimal;

import com.rosewood.payroll.exceptions.EmployeeNotFoundException;
import com.rosewood.payroll.exceptions.PayrollException;

//Console demonstration using sample data only; it is not part of the payroll library.
public class Demo {

    private static final double STANDARD_HOURS = 40;
    private static final double OVERTIME_HOURS = 50;
    private static final String EFUA_HOURLY_RATE = "20";
    private static final String KWAME_HOURLY_RATE = "30";

    public static void main(String[] args) {
        PayrollCalculator calculator = new PayrollCalculator(
                new InMemoryEmployeeRepository(),
                new GrossPayCalculator(),
                new TaxCalculator(StandardTaxBands.create()));

        report(calculator, "e1", STANDARD_HOURS, EFUA_HOURLY_RATE);
        report(calculator, "e2", OVERTIME_HOURS, KWAME_HOURLY_RATE);
        report(calculator, "unknown", STANDARD_HOURS, EFUA_HOURLY_RATE);
    }

    private static void report(
            PayrollCalculator calculator, String employeeId, double hours, String rate) {
        try {
            Payslip payslip = calculator.calculatePayslip(employeeId, hours, new BigDecimal(rate));
            System.out.println("Net pay for " + payslip.employeeName() + ": GHS " + payslip.net());
            System.out.println("Result " + employeeId + " -> " + payslip.net());
        } catch (EmployeeNotFoundException e) {
            System.out.println("Result " + employeeId + " -> UNKNOWN EMPLOYEE");
        } catch (PayrollException e) {
            System.out.println("Result " + employeeId + " -> NOT PAID: " + e.getMessage());
        }
    }
}