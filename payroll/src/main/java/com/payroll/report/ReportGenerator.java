package com.payroll.report;

import com.payroll.model.Employee;
import com.payroll.model.EmployeeType;
import com.payroll.model.Payslip;
import com.payroll.util.Money;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Text and CSV reports using Java I/O and the Collections/Streams API. */
public final class ReportGenerator {
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String LINE = "=".repeat(56) + "\n";
    private static final String THIN = "-".repeat(56) + "\n";

    private ReportGenerator() {
    }

    public static String payslipText(Payslip p) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE).append(center("PAYSLIP - " + p.month().format(MONTH).toUpperCase())).append(LINE);
        sb.append(String.format("Employee : %s (%s)%n", p.employeeName(), p.empCode()));
        sb.append(String.format("Category : %s%n", p.type()));
        sb.append(THIN);
        row(sb, "Basic pay", p.basicPay());
        p.allowanceBreakdown().forEach((k, v) -> row(sb, "  + " + k, v));
        row(sb, "Total allowances", p.totalAllowances());
        sb.append(THIN);
        row(sb, "GROSS SALARY", p.grossSalary());
        sb.append(THIN);
        p.deductionBreakdown().forEach((k, v) -> row(sb, "  - " + k, v));
        row(sb, "Total deductions", p.totalDeductions());
        sb.append(LINE);
        row(sb, "NET SALARY", p.netSalary());
        sb.append(LINE);
        return sb.toString();
    }

    public static String summaryReport(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE).append(center("PAYROLL SUMMARY (current salaries)"))
          .append(center("Generated " + LocalDateTime.now().format(STAMP))).append(LINE);
        if (employees.isEmpty()) {
            return sb.append("No employees.\n").toString();
        }

        // Department-wise (TreeMap keeps departments sorted)
        Map<String, List<Employee>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.toList()));
        sb.append("\nBY DEPARTMENT\n").append(String.format("%-18s %5s %16s %16s%n", "Department", "Count", "Gross", "Net"));
        byDept.forEach((dept, list) -> sb.append(String.format("%-18s %5d %16s %16s%n",
                trim(dept, 18), list.size(), Money.format(total(list, true)), Money.format(total(list, false)))));

        // Category-wise (EnumMap keeps enum order)
        Map<EmployeeType, Long> byType = employees.stream()
                .collect(Collectors.groupingBy(Employee::getType, () -> new EnumMap<>(EmployeeType.class),
                        Collectors.counting()));
        sb.append("\nBY CATEGORY\n");
        byType.forEach((type, count) -> sb.append(String.format("%-18s %5d%n", type, count)));

        BigDecimal totalNet = total(employees, false);
        Employee top = employees.stream().max(Comparator.comparing(Employee::calculateNetSalary)).orElseThrow();
        sb.append("\n").append(THIN);
        sb.append(String.format("%-36s %19d%n", "Total employees", employees.size()));
        row(sb, "Total monthly gross", total(employees, true));
        row(sb, "Total monthly net", totalNet);
        row(sb, "Average net salary",
                totalNet.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP));
        sb.append(String.format("Highest net pay     : %s (%s)%n", top.getName(), Money.format(top.calculateNetSalary())));
        return sb.toString();
    }

    public static void writeText(String content, Path file) throws IOException {
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    public static void exportPayslipsCsv(List<Payslip> slips, Path file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("Month,Code,Name,Category,Basic,Allowances,Gross,Deductions,Net");
            w.newLine();
            for (Payslip p : slips) {
                w.write(String.join(",",
                        p.month().toString(), csv(p.empCode()), csv(p.employeeName()), csv(p.type().toString()),
                        p.basicPay().toPlainString(), p.totalAllowances().toPlainString(),
                        p.grossSalary().toPlainString(), p.totalDeductions().toPlainString(),
                        p.netSalary().toPlainString()));
                w.newLine();
            }
        }
    }

    // ---- helpers ----

    private static BigDecimal total(List<Employee> list, boolean gross) {
        return list.stream()
                .map(e -> gross ? e.calculateGrossSalary() : e.calculateNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static StringBuilder row(StringBuilder sb, String label, BigDecimal value) {
        return sb.append(String.format("%-36s %19s%n", label, value == null ? "" : Money.format(value)));
    }

    private static String center(String text) {
        int pad = Math.max(0, (56 - text.length()) / 2);
        return " ".repeat(pad) + text + "\n";
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
