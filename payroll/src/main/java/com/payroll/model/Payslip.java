package com.payroll.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

/** Immutable snapshot of one employee's pay for one month. */
public record Payslip(
        int employeeId,
        String empCode,
        String employeeName,
        EmployeeType type,
        YearMonth month,
        BigDecimal basicPay,
        Map<String, BigDecimal> allowanceBreakdown,
        BigDecimal totalAllowances,
        BigDecimal grossSalary,
        Map<String, BigDecimal> deductionBreakdown,
        BigDecimal totalDeductions,
        BigDecimal netSalary) {
}
