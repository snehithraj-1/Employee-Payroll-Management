package com.payroll.exception;

/** Base checked exception for everything that can go wrong in the payroll system. */
public class PayrollException extends Exception {
    public PayrollException(String message) {
        super(message);
    }

    public PayrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
