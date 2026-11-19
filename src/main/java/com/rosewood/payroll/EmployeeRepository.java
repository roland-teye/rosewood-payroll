package com.rosewood.payroll;

import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(String id);
}