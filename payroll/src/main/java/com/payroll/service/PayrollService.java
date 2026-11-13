package com.payroll.service;

import com.payroll.dao.EmployeeDAO;
import com.payroll.dao.EmployeeDAOImpl;
import com.payroll.dao.PayslipDAO;
import com.payroll.exception.EmployeeNotFoundException;
import com.payroll.exception.PayrollException;
import com.payroll.exception.ValidationException;
import com.payroll.model.Employee;
import com.payroll.model.Payslip;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Business rules live here. The UI only talks to this class. */
public class PayrollService {
    private final EmployeeDAO employeeDAO;
    private final PayslipDAO payslipDAO;

    public PayrollService() {
        this(new EmployeeDAOImpl(), new PayslipDAO());
    }

    public PayrollService(EmployeeDAO employeeDAO, PayslipDAO payslipDAO) {
        this.employeeDAO = employeeDAO;
        this.payslipDAO = payslipDAO;
    }

    // ---- Employees ----

    public List<Employee> getAllEmployees() throws PayrollException {
        return employeeDAO.findAll();
    }

    public List<Employee> searchEmployees(String keyword) throws PayrollException {
        return (keyword == null || keyword.isBlank()) ? employeeDAO.findAll() : employeeDAO.search(keyword);
    }

    public void addEmployee(Employee e) throws PayrollException {
        e.validate();
        checkUnique(e);
        employeeDAO.add(e);
    }

    public void updateEmployee(Employee e) throws PayrollException {
        if (e.getId() <= 0) {
            throw new EmployeeNotFoundException(e.getId());
        }
        e.validate();
        checkUnique(e);
        if (!employeeDAO.update(e)) {
            throw new EmployeeNotFoundException(e.getId());
        }
    }

    public void deleteEmployee(int id) throws PayrollException {
        if (!employeeDAO.delete(id)) {
            throw new EmployeeNotFoundException(id);
        }
    }

    private void checkUnique(Employee e) throws PayrollException {
        List<String> errors = new ArrayList<>();
        if (employeeDAO.codeExists(e.getEmpCode(), e.getId()))
            errors.add("Employee code " + e.getEmpCode() + " is already used.");
        if (employeeDAO.emailExists(e.getEmail(), e.getId()))
            errors.add("Email " + e.getEmail() + " is already used.");
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // ---- Payroll ----

    /** Generates (or regenerates) payslips for everyone who had joined by the end of the month. */
    public List<Payslip> runPayroll(YearMonth month) throws PayrollException {
        if (month.isAfter(YearMonth.now())) {
            throw new ValidationException(List.of("Payroll cannot be run for a future month."));
        }
        List<Payslip> slips = employeeDAO.findAll().stream()
                .filter(e -> !e.getJoinDate().isAfter(month.atEndOfMonth()))
                .map(e -> e.generatePayslip(month))   // polymorphic call: each subclass computes its own pay
                .toList();
        if (slips.isEmpty()) {
            throw new ValidationException(List.of("No employees had joined by " + month + "."));
        }
        payslipDAO.saveAll(slips);
        return slips;
    }

    public List<Payslip> getPayslips(YearMonth month) throws PayrollException {
        return payslipDAO.findByMonth(month);
    }
}
