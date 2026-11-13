package com.payroll.model;

import com.payroll.util.InputValidator;
import com.payroll.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fixed monthly contract fee. No allowances; flat 10% TDS deducted. */
public class ContractEmployee extends Employee {
    private static final double TDS_RATE = 10;

    private final BigDecimal contractAmount;

    public ContractEmployee(int id, String empCode, String name, String email, String phone,
                            String department, String designation, LocalDate joinDate,
                            BigDecimal contractAmount) {
        super(id, empCode, name, email, phone, department, designation, joinDate);
        this.contractAmount = contractAmount;
    }

    @Override
    public EmployeeType getType() {
        return EmployeeType.CONTRACT;
    }

    @Override
    public BigDecimal calculateBasicPay() {
        return contractAmount;
    }

    @Override
    protected Map<String, BigDecimal> allowanceComponents() {
        return new LinkedHashMap<>();
    }

    @Override
    protected Map<String, BigDecimal> deductionComponents() {
        Map<String, BigDecimal> parts = new LinkedHashMap<>();
        parts.put("TDS (10%)", Money.percent(calculateGrossSalary(), TDS_RATE));
        return parts;
    }

    @Override
    protected void validateSpecific(List<String> errors) {
        if (!InputValidator.isPositive(contractAmount, 1_00_00_000))
            errors.add("Contract amount must be greater than 0 and at most 1,00,00,000.");
    }

    public BigDecimal getContractAmount() {
        return contractAmount;
    }
}
