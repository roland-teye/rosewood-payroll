package com.rosewood.payroll;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.rosewood.payroll.exceptions.InvalidInputException;

class InMemoryEmployeeRepositoryTest {

    private final InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();

    @Test
    void findsTheFirstSeededEmployee() {
        Optional<Employee> found = repository.findById("e1");

        assertTrue(found.isPresent());
        assertEquals("e1", found.get().id());
        assertEquals("Efua Sarpong", found.get().name());
    }

    @Test
    void findsTheSecondSeededEmployee() {
        Optional<Employee> found = repository.findById("e2");

        assertTrue(found.isPresent());
        assertEquals("Kwame Owusu", found.get().name());
    }

    @Test
    void returnsEmptyForAnUnknownId() {
        assertTrue(repository.findById("unknown").isEmpty());
    }

    @Test
    void idLookupIsCaseSensitive() {
        assertTrue(repository.findById("E1").isEmpty());
    }

    @Test
    void rejectsANullIdWithInvalidInputException() {
        InvalidInputException thrown =
                assertThrows(InvalidInputException.class, () -> repository.findById(null));

        assertEquals("Employee id must not be null", thrown.getMessage());
    }
}