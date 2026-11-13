package com.payroll.dao;

import com.payroll.exception.DataAccessException;
import com.payroll.model.EmployeeType;
import com.payroll.model.Payslip;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayslipDAO {

    private static final String UPSERT =
            "INSERT INTO payslips (employee_id, pay_month, basic_pay, allowances, gross_salary, deductions, "
            + "net_salary, allowance_breakdown, deduction_breakdown) VALUES (?,?,?,?,?,?,?,?,?) "
            + "ON DUPLICATE KEY UPDATE basic_pay=VALUES(basic_pay), allowances=VALUES(allowances), "
            + "gross_salary=VALUES(gross_salary), deductions=VALUES(deductions), net_salary=VALUES(net_salary), "
            + "allowance_breakdown=VALUES(allowance_breakdown), deduction_breakdown=VALUES(deduction_breakdown), "
            + "generated_at=CURRENT_TIMESTAMP";

    private static final String BY_MONTH =
            "SELECT p.*, e.emp_code, e.name, e.emp_type FROM payslips p "
            + "JOIN employees e ON e.id = p.employee_id WHERE p.pay_month=? ORDER BY e.emp_code";

    /** Saves all payslips in ONE transaction: either every payslip is saved or none is. */
    public void saveAll(List<Payslip> slips) throws DataAccessException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(UPSERT)) {
                for (Payslip p : slips) {
                    ps.setInt(1, p.employeeId());
                    ps.setString(2, p.month().toString());
                    ps.setBigDecimal(3, p.basicPay());
                    ps.setBigDecimal(4, p.totalAllowances());
                    ps.setBigDecimal(5, p.grossSalary());
                    ps.setBigDecimal(6, p.totalDeductions());
                    ps.setBigDecimal(7, p.netSalary());
                    ps.setString(8, encode(p.allowanceBreakdown()));
                    ps.setString(9, encode(p.deductionBreakdown()));
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not save payslips (nothing was saved)", ex);
        }
    }

    public List<Payslip> findByMonth(YearMonth month) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BY_MONTH)) {
            ps.setString(1, month.toString());
            List<Payslip> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Payslip(
                            rs.getInt("employee_id"),
                            rs.getString("emp_code"),
                            rs.getString("name"),
                            EmployeeType.valueOf(rs.getString("emp_type")),
                            YearMonth.parse(rs.getString("pay_month")),
                            rs.getBigDecimal("basic_pay"),
                            decode(rs.getString("allowance_breakdown")),
                            rs.getBigDecimal("allowances"),
                            rs.getBigDecimal("gross_salary"),
                            decode(rs.getString("deduction_breakdown")),
                            rs.getBigDecimal("deductions"),
                            rs.getBigDecimal("net_salary")));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not load payslips for " + month, ex);
        }
    }

    // Breakdown stored as "HRA (20% of basic)=10000.00|DA (10% of basic)=5000.00"
    private static String encode(Map<String, BigDecimal> map) {
        return map.entrySet().stream()
                .map(en -> en.getKey() + "=" + en.getValue().toPlainString())
                .collect(Collectors.joining("|"));
    }

    private static Map<String, BigDecimal> decode(String text) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return map;
        }
        for (String part : text.split("\\|")) {
            int eq = part.lastIndexOf('=');
            if (eq > 0) {
                map.put(part.substring(0, eq), new BigDecimal(part.substring(eq + 1)));
            }
        }
        return map;
    }
}
