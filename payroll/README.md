# Employee Payroll Management System

A desktop payroll app in **core Java** with a **JavaFX** GUI and **JDBC + MySQL** storage. No frameworks: just Java, JavaFX and the MySQL JDBC driver.

## Features
- Manage five employee categories: Full-Time, Manager, Part-Time, Contract, Intern
- Add / edit / delete / search employees (form fields change with the category)
- Calculate gross and net salary with a full allowance and deduction breakdown
- Run monthly payroll: payslips saved to MySQL in a single transaction
- View payslips, save one as `.txt`, export a whole month as `.csv`
- Summary report by department and category
- Input validation with every error shown at once; duplicate code/email checks

## Requirements
- JDK 21 or newer (works on later JDKs too)
- MySQL 8.x
- Maven 3.9+ (or the Maven bundled with IntelliJ IDEA)

## Setup
1. **Create the database**
   ```
   mysql -u root -p < database/schema.sql
   ```
   This creates `payroll_db`, both tables, and 5 sample employees.
2. **Set your MySQL password** in `src/main/resources/db.properties`.
3. **Run**
   ```
   mvn clean javafx:run
   ```
   In IntelliJ: open the folder as a Maven project, then run `com.payroll.Launcher`.

## Project structure
```
src/main/java/com/payroll/
├── App.java / Launcher.java      JavaFX entry point
├── model/                        OOP core
│   ├── Employee.java             abstract base (template methods)
│   ├── FullTimeEmployee.java
│   ├── Manager.java              extends FullTimeEmployee
│   ├── PartTimeEmployee.java
│   ├── ContractEmployee.java
│   ├── Intern.java
│   ├── EmployeeType.java, Payslip.java (record), PayDetails.java (record)
│   └── EmployeeFactory.java      builds the right subclass
├── dao/                          JDBC layer
│   ├── DBConnection.java
│   ├── EmployeeDAO.java          interface
│   ├── EmployeeDAOImpl.java      PreparedStatements
│   └── PayslipDAO.java           batch insert + transaction
├── service/PayrollService.java   business rules
├── report/ReportGenerator.java   text + CSV reports (Java I/O)
├── exception/                    custom checked exceptions
├── util/                         Money, TaxRules, InputValidator
└── ui/                           JavaFX tabs, form dialog, helpers
```

## Salary rules (simplified, for learning)
| Category  | Basic pay           | Allowances                                             | Deductions                              |
|-----------|---------------------|--------------------------------------------------------|-----------------------------------------|
| Full-Time | monthly salary      | HRA 20%, DA 10%, medical ₹1,250                         | PF 12% (on max ₹15,000), prof. tax, income tax |
| Manager   | monthly salary      | Full-Time allowances + management 15% + ₹500 per team member | same as Full-Time                  |
| Part-Time | hourly rate × hours | none                                                   | professional tax                        |
| Contract  | contract amount     | none                                                   | TDS 10%                                 |
| Intern    | stipend             | none                                                   | none                                    |

Income tax uses the new-regime slabs for FY 2025-26 with the ₹75,000 standard deduction, the 87A rebate up to ₹12 lakh taxable income and 4% cess. Surcharge and marginal relief are ignored. Professional tax follows Andhra Pradesh slabs.

`Gross = Basic + Allowances` and `Net = Gross − Deductions` are `final` methods in `Employee`, so subclasses can only change the components, never the formula.

## Where each concept is used
| Concept | Where |
|---|---|
| Inheritance | `Employee` → 4 subclasses; `Manager` → `FullTimeEmployee` (multilevel) |
| Runtime polymorphism | `calculateBasicPay()`, `allowanceComponents()`, `deductionComponents()`, `validateSpecific()` are overridden; `PayrollService.runPayroll()` calls them on a `List<Employee>` |
| Abstraction | abstract `Employee`, `EmployeeDAO` interface |
| Encapsulation | private fields, validation inside the model |
| Collections | `List`, `LinkedHashMap` (ordered breakdown), `TreeMap`, `EnumMap`, streams `groupingBy` |
| Exceptions | `PayrollException` hierarchy, try-with-resources, transaction rollback |
| JDBC | `PreparedStatement`, generated keys, batch updates, transactions |
| Java I/O | `Files`, `BufferedWriter` for payslip and CSV export |
| Design patterns | Factory, DAO, Template Method |

## Ideas for next steps
- Login with roles (admin / HR / employee view)
- Attendance and leave, with pro-rated salary for mid-month joiners
- Payslip PDF export
- JUnit tests for each salary class
- Connection pooling (HikariCP)
