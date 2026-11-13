package com.payroll.model;

import com.payroll.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fixed stipend, no allowances, no deductions. */
public class Intern extends Employee {
    private final BigDecimal stipend;

    public Intern(int id, String empCode, String name, String email, String phone,
                  String department, String designation, LocalDate joinDate,
                  BigDecimal stipend) {
        super(id, empCode, name, email, phone, department, designation, joinDate);
        this.stipend = stipend;
    }

    @Override
    public EmployeeType getType() {
        return EmployeeType.INTERN;
    }

    @Override
    public BigDecimal calculateBasicPay() {
        return stipend;
    }

    @Override
    protected Map<String, BigDecimal> allowanceComponents() {
        return new LinkedHashMap<>();
    }

    @Override
    protected Map<String, BigDecimal> deductionComponents() {
        return new LinkedHashMap<>();
    }

    @Override
    protected void validateSpecific(List<String> errors) {
        if (!InputValidator.inRange(stipend, 0, 1_00_000))
            errors.add("Stipend must be between 0 and 1,00,000.");
    }

    public BigDecimal getStipend() {
        return stipend;
    }
}
