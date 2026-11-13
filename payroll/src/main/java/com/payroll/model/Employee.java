package com.payroll.model;

import com.payroll.exception.ValidationException;
import com.payroll.util.InputValidator;
import com.payroll.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for every employee category.
 *
 * Subclasses decide HOW basic pay, allowances and deductions are calculated
 * (runtime polymorphism). This class fixes WHAT gross and net salary mean
 * (template method), so the formula can never be broken by a subclass.
 */
public abstract class Employee {
    private int id;
    private final String empCode;
    private final String name;
    private final String email;
    private final String phone;
    private final String department;
    private final String designation;
    private final LocalDate joinDate;

    protected Employee(int id, String empCode, String name, String email, String phone,
                       String department, String designation, LocalDate joinDate) {
        this.id = id;
        this.empCode = empCode == null ? null : empCode.trim().toUpperCase();
        this.name = name == null ? null : name.trim();
        this.email = email == null ? null : email.trim().toLowerCase();
        this.phone = phone == null ? null : phone.trim();
        this.department = department == null ? null : department.trim();
        this.designation = designation == null ? null : designation.trim();
        this.joinDate = joinDate;
    }

    // ---- What each subclass must provide (overridden per role) ----

    public abstract EmployeeType getType();

    public abstract BigDecimal calculateBasicPay();

    /** Named allowance components, in display order. Must return a mutable map so subclasses can extend it. */
    protected abstract Map<String, BigDecimal> allowanceComponents();

    /** Named deduction components, in display order. */
    protected abstract Map<String, BigDecimal> deductionComponents();

    /** Category-specific validation (salary, hours, team size...). */
    protected abstract void validateSpecific(List<String> errors);

    // ---- Fixed formulas (template methods) ----

    public final BigDecimal calculateAllowances() {
        return sum(allowanceComponents());
    }

    public final BigDecimal calculateGrossSalary() {
        return Money.round(calculateBasicPay().add(calculateAllowances()));
    }

    public final BigDecimal calculateDeductions() {
        return sum(deductionComponents());
    }

    public final BigDecimal calculateNetSalary() {
        return Money.round(calculateGrossSalary().subtract(calculateDeductions()));
    }

    public final Payslip generatePayslip(YearMonth month) {
        return new Payslip(id, empCode, name, getType(), month,
                Money.round(calculateBasicPay()),
                Collections.unmodifiableMap(allowanceComponents()), calculateAllowances(),
                calculateGrossSalary(),
                Collections.unmodifiableMap(deductionComponents()), calculateDeductions(),
                calculateNetSalary());
    }

    /** Collects every problem with this employee and throws them together. */
    public final void validate() throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (!InputValidator.isValidEmpCode(empCode))
            errors.add("Employee code must be 2-4 capital letters followed by 3-6 digits (e.g. EMP001).");
        if (!InputValidator.isValidName(name))
            errors.add("Name must be 2-100 characters: letters, spaces, . ' - only.");
        if (!InputValidator.isValidEmail(email))
            errors.add("Email address is not valid.");
        if (!InputValidator.isValidPhone(phone))
            errors.add("Phone must be a 10-digit Indian mobile number starting with 6-9.");
        if (InputValidator.isBlank(department))
            errors.add("Department is required.");
        if (InputValidator.isBlank(designation))
            errors.add("Designation is required.");
        if (joinDate == null)
            errors.add("Join date is required.");
        else if (joinDate.isAfter(LocalDate.now()))
            errors.add("Join date cannot be in the future.");

        validateSpecific(errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static BigDecimal sum(Map<String, BigDecimal> parts) {
        return Money.round(parts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    // ---- Getters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmpCode() { return empCode; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public LocalDate getJoinDate() { return joinDate; }

    @Override
    public String toString() {
        return empCode + " - " + name + " (" + getType() + ")";
    }
}
