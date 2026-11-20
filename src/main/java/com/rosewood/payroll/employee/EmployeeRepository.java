package com.rosewood.payroll.employee;

import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(String id);
}