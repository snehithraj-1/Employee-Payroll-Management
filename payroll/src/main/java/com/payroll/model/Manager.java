package com.payroll.model;

import com.payroll.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** A full-time employee who also gets management and team allowances (overrides + extends parent rules). */
public class Manager extends FullTimeEmployee {
    private static final long TEAM_ALLOWANCE_PER_MEMBER = 500;

    private final int teamSize;

    public Manager(int id, String empCode, String name, String email, String phone,
                   String department, String designation, LocalDate joinDate,
                   BigDecimal monthlySalary, int teamSize) {
        super(id, empCode, name, email, phone, department, designation, joinDate, monthlySalary);
        this.teamSize = teamSize;
    }

    @Override
    public EmployeeType getType() {
        return EmployeeType.MANAGER;
    }

    @Override
    protected Map<String, BigDecimal> allowanceComponents() {
        Map<String, BigDecimal> parts = super.allowanceComponents(); // reuse HRA, DA, medical
        parts.put("Management allowance (15%)", Money.percent(calculateBasicPay(), 15));
        parts.put("Team allowance (500 x " + teamSize + ")", Money.of(TEAM_ALLOWANCE_PER_MEMBER * teamSize));
        return parts;
    }

    @Override
    protected void validateSpecific(List<String> errors) {
        super.validateSpecific(errors);
        if (teamSize < 1 || teamSize > 500)
            errors.add("Team size must be between 1 and 500.");
    }

    public int getTeamSize() {
        return teamSize;
    }
}
