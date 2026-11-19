package com.rosewood.payroll;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> employees = new HashMap<>();

    public InMemoryEmployeeRepository() {
        employees.put("e1", new Employee("e1", "Efua Sarpong"));
        employees.put("e2", new Employee("e2", "Kwame Owusu"));
    }

    @Override
    public Optional<Employee> findById(String id) {
        return Optional.ofNullable(employees.get(id));
    }
}
