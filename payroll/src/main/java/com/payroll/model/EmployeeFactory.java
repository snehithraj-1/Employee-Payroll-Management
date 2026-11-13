package com.payroll.model;

import java.time.LocalDate;

/** Factory: builds the right Employee subclass for a given type. */
public final class EmployeeFactory {
    private EmployeeFactory() {
    }

    public static Employee create(EmployeeType type, int id, String code, String name, String email,
                                  String phone, String department, String designation,
                                  LocalDate joinDate, PayDetails pay) {
        return switch (type) {
            case FULL_TIME -> new FullTimeEmployee(id, code, name, email, phone, department, designation,
                    joinDate, pay.monthlySalary());
            case MANAGER -> new Manager(id, code, name, email, phone, department, designation,
                    joinDate, pay.monthlySalary(), pay.teamSize() == null ? 0 : pay.teamSize());
            case PART_TIME -> new PartTimeEmployee(id, code, name, email, phone, department, designation,
                    joinDate, pay.hourlyRate(), pay.hoursWorked());
            case CONTRACT -> new ContractEmployee(id, code, name, email, phone, department, designation,
                    joinDate, pay.contractAmount());
            case INTERN -> new Intern(id, code, name, email, phone, department, designation,
                    joinDate, pay.stipend());
        };
    }
}
