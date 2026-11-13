package com.payroll.exception;

/** Wraps SQLExceptions so the UI never has to deal with JDBC details. */
public class DataAccessException extends PayrollException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
