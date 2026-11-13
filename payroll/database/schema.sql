-- Employee Payroll Management System - MySQL schema
-- Run once:  mysql -u root -p < database/schema.sql

CREATE DATABASE IF NOT EXISTS payroll_db;
USE payroll_db;

-- One table for every employee category (single-table inheritance).
-- Columns that don't apply to a category stay NULL.
CREATE TABLE IF NOT EXISTS employees (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    emp_code        VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    phone           VARCHAR(15)  NOT NULL,
    department      VARCHAR(60)  NOT NULL,
    designation     VARCHAR(60)  NOT NULL,
    join_date       DATE         NOT NULL,
    emp_type        ENUM('FULL_TIME','MANAGER','PART_TIME','CONTRACT','INTERN') NOT NULL,
    monthly_salary  DECIMAL(12,2) NULL,   -- FULL_TIME, MANAGER
    hourly_rate     DECIMAL(10,2) NULL,   -- PART_TIME
    hours_worked    DECIMAL(6,2)  NULL,   -- PART_TIME
    contract_amount DECIMAL(12,2) NULL,   -- CONTRACT
    stipend         DECIMAL(10,2) NULL,   -- INTERN
    team_size       INT           NULL,   -- MANAGER
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- One payslip per employee per month (re-running payroll overwrites it).
CREATE TABLE IF NOT EXISTS payslips (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    employee_id         INT          NOT NULL,
    pay_month           CHAR(7)      NOT NULL,          -- e.g. 2026-10
    basic_pay           DECIMAL(12,2) NOT NULL,
    allowances          DECIMAL(12,2) NOT NULL,
    gross_salary        DECIMAL(12,2) NOT NULL,
    deductions          DECIMAL(12,2) NOT NULL,
    net_salary          DECIMAL(12,2) NOT NULL,
    allowance_breakdown TEXT,
    deduction_breakdown TEXT,
    generated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_employee_month (employee_id, pay_month),
    CONSTRAINT fk_payslip_employee FOREIGN KEY (employee_id)
        REFERENCES employees(id) ON DELETE CASCADE
);

-- Sample data: one employee per category
INSERT IGNORE INTO employees
    (emp_code, name, email, phone, department, designation, join_date, emp_type,
     monthly_salary, hourly_rate, hours_worked, contract_amount, stipend, team_size)
VALUES
    ('EMP001', 'Ananya Rao',    'ananya.rao@company.com',    '9876543210', 'Engineering', 'Software Engineer',   '2024-07-01', 'FULL_TIME', 85000.00, NULL,   NULL,   NULL,     NULL,     NULL),
    ('EMP002', 'Vikram Reddy',  'vikram.reddy@company.com',  '9876543211', 'Engineering', 'Engineering Manager', '2021-03-15', 'MANAGER',  150000.00, NULL,   NULL,   NULL,     NULL,     8),
    ('EMP003', 'Sneha Varma',   'sneha.varma@company.com',   '9876543212', 'Support',     'Support Associate',   '2025-01-10', 'PART_TIME', NULL,     350.00, 80.00,  NULL,     NULL,     NULL),
    ('EMP004', 'Rahul Naidu',   'rahul.naidu@company.com',   '9876543213', 'Design',      'UI Consultant',       '2025-06-01', 'CONTRACT',  NULL,     NULL,   NULL,   60000.00, NULL,     NULL),
    ('EMP005', 'Divya Krishna', 'divya.krishna@company.com', '9876543214', 'Engineering', 'ML Intern',           '2026-05-20', 'INTERN',    NULL,     NULL,   NULL,   NULL,     20000.00, NULL);
