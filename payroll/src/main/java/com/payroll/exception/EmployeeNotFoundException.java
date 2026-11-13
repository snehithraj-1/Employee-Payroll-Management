package com.payroll.exception;

public class EmployeeNotFoundException extends PayrollException {
    public EmployeeNotFoundException(int id) {
        super("No employee found with id " + id + ". It may have been deleted.");
    }
}
