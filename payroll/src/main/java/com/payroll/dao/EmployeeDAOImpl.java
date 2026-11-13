package com.payroll.dao;

import com.payroll.exception.DataAccessException;
import com.payroll.model.Employee;
import com.payroll.model.EmployeeFactory;
import com.payroll.model.EmployeeType;
import com.payroll.model.PayDetails;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation using PreparedStatements (safe from SQL injection). */
public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String COLUMNS =
            "emp_code, name, email, phone, department, designation, join_date, emp_type, "
            + "monthly_salary, hourly_rate, hours_worked, contract_amount, stipend, team_size";

    private static final String INSERT =
            "INSERT INTO employees (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE =
            "UPDATE employees SET emp_code=?, name=?, email=?, phone=?, department=?, designation=?, "
            + "join_date=?, emp_type=?, monthly_salary=?, hourly_rate=?, hours_worked=?, "
            + "contract_amount=?, stipend=?, team_size=? WHERE id=?";
    private static final String DELETE = "DELETE FROM employees WHERE id=?";
    private static final String SELECT_ALL = "SELECT * FROM employees ORDER BY emp_code";
    private static final String SELECT_BY_ID = "SELECT * FROM employees WHERE id=?";
    private static final String SEARCH =
            "SELECT * FROM employees WHERE emp_code LIKE ? OR name LIKE ? OR department LIKE ? "
            + "OR designation LIKE ? ORDER BY emp_code";
    private static final String CODE_EXISTS = "SELECT 1 FROM employees WHERE emp_code=? AND id<>?";
    private static final String EMAIL_EXISTS = "SELECT 1 FROM employees WHERE email=? AND id<>?";

    @Override
    public void add(Employee e) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not add employee " + e.getEmpCode(), ex);
        }
    }

    @Override
    public boolean update(Employee e) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE)) {
            int next = bind(ps, e);
            ps.setInt(next, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update employee " + e.getEmpCode(), ex);
        }
    }

    @Override
    public boolean delete(int id) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete employee", ex);
        }
    }

    @Override
    public Optional<Employee> findById(int id) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not load employee", ex);
        }
    }

    @Override
    public List<Employee> findAll() throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            return mapAll(rs);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not load employees", ex);
        }
    }

    @Override
    public List<Employee> search(String keyword) throws DataAccessException {
        String like = "%" + keyword.trim() + "%";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SEARCH)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Search failed", ex);
        }
    }

    @Override
    public boolean codeExists(String empCode, int excludeId) throws DataAccessException {
        return exists(CODE_EXISTS, empCode, excludeId);
    }

    @Override
    public boolean emailExists(String email, int excludeId) throws DataAccessException {
        return exists(EMAIL_EXISTS, email, excludeId);
    }

    // ---- helpers ----

    private boolean exists(String sql, String value, int excludeId) throws DataAccessException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Duplicate check failed", ex);
        }
    }

    /** Binds the 14 employee columns and returns the next parameter index. */
    private int bind(PreparedStatement ps, Employee e) throws SQLException {
        PayDetails pay = PayDetails.from(e);
        int i = 1;
        ps.setString(i++, e.getEmpCode());
        ps.setString(i++, e.getName());
        ps.setString(i++, e.getEmail());
        ps.setString(i++, e.getPhone());
        ps.setString(i++, e.getDepartment());
        ps.setString(i++, e.getDesignation());
        ps.setDate(i++, Date.valueOf(e.getJoinDate()));
        ps.setString(i++, e.getType().name());
        ps.setObject(i++, pay.monthlySalary(), Types.DECIMAL);
        ps.setObject(i++, pay.hourlyRate(), Types.DECIMAL);
        ps.setObject(i++, pay.hoursWorked(), Types.DECIMAL);
        ps.setObject(i++, pay.contractAmount(), Types.DECIMAL);
        ps.setObject(i++, pay.stipend(), Types.DECIMAL);
        ps.setObject(i++, pay.teamSize(), Types.INTEGER);
        return i;
    }

    private List<Employee> mapAll(ResultSet rs) throws SQLException {
        List<Employee> list = new ArrayList<>();
        while (rs.next()) {
            list.add(map(rs));
        }
        return list;
    }

    private Employee map(ResultSet rs) throws SQLException {
        PayDetails pay = new PayDetails(
                rs.getBigDecimal("monthly_salary"),
                rs.getBigDecimal("hourly_rate"),
                rs.getBigDecimal("hours_worked"),
                rs.getBigDecimal("contract_amount"),
                rs.getBigDecimal("stipend"),
                rs.getObject("team_size", Integer.class));
        return EmployeeFactory.create(
                EmployeeType.valueOf(rs.getString("emp_type")),
                rs.getInt("id"),
                rs.getString("emp_code"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("department"),
                rs.getString("designation"),
                rs.getDate("join_date").toLocalDate(),
                pay);
    }
}
