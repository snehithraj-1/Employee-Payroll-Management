package com.payroll.model;

import java.math.BigDecimal;

/**
 * The category-specific pay fields, gathered in one place.
 * Used by the DAO (to read/write DB columns) and the form (to prefill fields).
 * Fields that don't apply to a category are null.
 */
public record PayDetails(BigDecimal monthlySalary,
                         BigDecimal hourlyRate,
                         BigDecimal hoursWorked,
                         BigDecimal contractAmount,
                         BigDecimal stipend,
                         Integer teamSize) {

    public static PayDetails from(Employee e) {
        BigDecimal monthly = null, rate = null, hours = null, contract = null, stipend = null;
        Integer team = null;
        if (e instanceof FullTimeEmployee f) monthly = f.getMonthlySalary();   // includes Manager
        if (e instanceof Manager m) team = m.getTeamSize();
        if (e instanceof PartTimeEmployee p) {
            rate = p.getHourlyRate();
            hours = p.getHoursWorked();
        }
        if (e instanceof ContractEmployee c) contract = c.getContractAmount();
        if (e instanceof Intern i) stipend = i.getStipend();
        return new PayDetails(monthly, rate, hours, contract, stipend, team);
    }
}
