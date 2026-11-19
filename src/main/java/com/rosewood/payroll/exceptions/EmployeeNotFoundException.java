package com.rosewood.payroll.exceptions;

public class EmployeeNotFoundException extends PayrollException {

    public EmployeeNotFoundException(String employeeId) {
        super("Employee '" + employeeId + "' was not found");
    }
}