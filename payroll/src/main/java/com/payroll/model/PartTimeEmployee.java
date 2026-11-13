package com.payroll.model;

import com.payroll.util.InputValidator;
import com.payroll.util.Money;
import com.payroll.util.TaxRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Paid by the hour. No allowances; only professional tax applies. */
public class PartTimeEmployee extends Employee {
    private final BigDecimal hourlyRate;
    private final BigDecimal hoursWorked;

    public PartTimeEmployee(int id, String empCode, String name, String email, String phone,
                            String department, String designation, LocalDate joinDate,
                            BigDecimal hourlyRate, BigDecimal hoursWorked) {
        super(id, empCode, name, email, phone, department, designation, joinDate);
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }

    @Override
    public EmployeeType getType() {
        return EmployeeType.PART_TIME;
    }

    @Override
    public BigDecimal calculateBasicPay() {
        return Money.round(hourlyRate.multiply(hoursWorked));
    }

    @Override
    protected Map<String, BigDecimal> allowanceComponents() {
        return new LinkedHashMap<>();
    }

    @Override
    protected Map<String, BigDecimal> deductionComponents() {
        Map<String, BigDecimal> parts = new LinkedHashMap<>();
        parts.put("Professional tax", TaxRules.professionalTax(calculateGrossSalary()));
        return parts;
    }

    @Override
    protected void validateSpecific(List<String> errors) {
        if (!InputValidator.isPositive(hourlyRate, 10_000))
            errors.add("Hourly rate must be greater than 0 and at most 10,000.");
        if (!InputValidator.inRange(hoursWorked, 0, 300))
            errors.add("Hours worked must be between 0 and 300 for the month.");
    }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getHoursWorked() { return hoursWorked; }
}
