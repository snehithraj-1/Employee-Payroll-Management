package com.payroll.dao;

import com.payroll.exception.DataAccessException;
import com.payroll.model.Employee;

import java.util.List;
import java.util.Optional;

/** Data-access contract. The service depends on this interface, not on JDBC. */
public interface EmployeeDAO {
    void add(Employee employee) throws DataAccessException;

    boolean update(Employee employee) throws DataAccessException;

    boolean delete(int id) throws DataAccessException;

    Optional<Employee> findById(int id) throws DataAccessException;

    List<Employee> findAll() throws DataAccessException;

    List<Employee> search(String keyword) throws DataAccessException;

    boolean codeExists(String empCode, int excludeId) throws DataAccessException;

    boolean emailExists(String email, int excludeId) throws DataAccessException;
}
