package com.payroll.exception;

import java.util.List;

/** Thrown when user input is invalid. Carries every problem found, not just the first. */
public class ValidationException extends PayrollException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
