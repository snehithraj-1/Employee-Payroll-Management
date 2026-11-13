package com.payroll.model;

public enum EmployeeType {
    FULL_TIME("Full-Time"),
    MANAGER("Manager"),
    PART_TIME("Part-Time"),
    CONTRACT("Contract"),
    INTERN("Intern");

    private final String label;

    EmployeeType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
