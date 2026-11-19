package com.rosewood.payroll.exceptions;

public class EmployeeLookupException extends PayrollException {

    public EmployeeLookupException(String employeeId, Throwable cause) {
        super("Could not look up employee '" + employeeId + "'", cause);
    }
}