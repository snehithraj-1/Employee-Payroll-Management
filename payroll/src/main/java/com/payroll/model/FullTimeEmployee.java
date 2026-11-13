package com.payroll.model;

import com.payroll.util.InputValidator;
import com.payroll.util.Money;
import com.payroll.util.TaxRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Salaried employee: HRA + DA + medical allowance; PF, professional tax and income tax deducted. */
public class FullTimeEmployee extends Employee {
    protected static final BigDecimal MEDICAL_ALLOWANCE = Money.of(1250);

    private final BigDecimal monthlySalary;

    public FullTimeEmployee(int id, String empCode, String name, String email, String phone,
                            String department, String designation, LocalDate joinDate,
                            BigDecimal monthlySalary) {
        super(id, empCode, name, email, phone, department, designation, joinDate);
        this.monthlySalary = monthlySalary;
    }

    @Override
    public EmployeeType getType() {
        return EmployeeType.FULL_TIME;
    }

    @Override
    public BigDecimal calculateBasicPay() {
        return monthlySalary;
    }

    @Override
    protected Map<String, BigDecimal> allowanceComponents() {
        BigDecimal basic = calculateBasicPay();
        Map<String, BigDecimal> parts = new LinkedHashMap<>();
        parts.put("HRA (20% of basic)", Money.percent(basic, 20));
        parts.put("DA (10% of basic)", Money.percent(basic, 10));
        parts.put("Medical allowance", MEDICAL_ALLOWANCE);
        return parts;
    }

    @Override
    protected Map<String, BigDecimal> deductionComponents() {
        BigDecimal gross = calculateGrossSalary();
        Map<String, BigDecimal> parts = new LinkedHashMap<>();
        parts.put("Provident fund (12%)", TaxRules.providentFund(calculateBasicPay()));
        parts.put("Professional tax", TaxRules.professionalTax(gross));
        parts.put("Income tax (TDS)", TaxRules.monthlyIncomeTax(gross));
        return parts;
    }

    @Override
    protected void validateSpecific(List<String> errors) {
        if (!InputValidator.isPositive(monthlySalary, 1_00_00_000))
            errors.add("Monthly salary must be greater than 0 and at most 1,00,00,000.");
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }
}
