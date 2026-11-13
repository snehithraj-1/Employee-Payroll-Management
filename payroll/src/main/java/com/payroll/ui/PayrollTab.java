package com.payroll.ui;

import com.payroll.model.Payslip;
import com.payroll.report.ReportGenerator;
import com.payroll.service.PayrollService;
import com.payroll.util.Money;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Function;

public class PayrollTab extends Tab {
    private final PayrollService service;
    private final ObservableList<Payslip> data = FXCollections.observableArrayList();
    private final TableView<Payslip> table = new TableView<>(data);
    private final ComboBox<Month> monthBox = new ComboBox<>(FXCollections.observableArrayList(Month.values()));
    private final Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, Year.now().getValue());
    private final Label totals = new Label();

    public PayrollTab(PayrollService service) {
        super("Payroll");
        this.service = service;

        monthBox.setValue(YearMonth.now().getMonth());
        monthBox.setConverter(new StringConverter<>() {
            @Override public String toString(Month m) { return m == null ? "" : m.getDisplayName(TextStyle.FULL, Locale.ENGLISH); }
            @Override public Month fromString(String s) { return null; }
        });
        yearSpinner.setPrefWidth(90);

        Button run = new Button("Run Payroll");
        Button load = new Button("Load Saved");
        Button view = new Button("View Payslip");
        Button save = new Button("Save Payslip (.txt)");
        Button export = new Button("Export Month (.csv)");
        run.getStyleClass().add("primary");

        var noSelection = table.getSelectionModel().selectedItemProperty().isNull();
        view.disableProperty().bind(noSelection);
        save.disableProperty().bind(noSelection);

        run.setOnAction(e -> onRun());
        load.setOnAction(e -> onLoad());
        view.setOnAction(e -> onView());
        save.setOnAction(e -> onSavePayslip());
        export.setOnAction(e -> onExport());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8, new Label("Month:"), monthBox, yearSpinner, run, load, spacer, view, save, export);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        buildTable();
        totals.getStyleClass().add("totals");

        VBox root = new VBox(10, toolbar, table, totals);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setPadding(new Insets(12));
        setContent(root);
    }

    private void buildTable() {
        table.getColumns().add(col("Code", Payslip::empCode));
        table.getColumns().add(col("Name", Payslip::employeeName));
        table.getColumns().add(col("Category", p -> p.type().toString()));
        table.getColumns().add(col("Basic", p -> Money.format(p.basicPay())));
        table.getColumns().add(col("Allowances", p -> Money.format(p.totalAllowances())));
        table.getColumns().add(col("Gross", p -> Money.format(p.grossSalary())));
        table.getColumns().add(col("Deductions", p -> Money.format(p.totalDeductions())));
        table.getColumns().add(col("Net Pay", p -> Money.format(p.netSalary())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Pick a month, then click 'Run Payroll' or 'Load Saved'."));
    }

    private static TableColumn<Payslip, String> col(String title, Function<Payslip, String> getter) {
        TableColumn<Payslip, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(getter.apply(cd.getValue())));
        return c;
    }

    private YearMonth selectedMonth() {
        return YearMonth.of(yearSpinner.getValue(), monthBox.getValue());
    }

    private void onRun() {
        YearMonth ym = selectedMonth();
        if (!Alerts.confirm("Run payroll for " + ym + "?",
                "Payslips will be generated for all eligible employees.\nExisting payslips for this month will be replaced.")) {
            return;
        }
        Async.run(() -> service.runPayroll(ym), slips -> {
            show(slips);
            Alerts.info("Payroll complete", slips.size() + " payslip(s) generated for " + ym + ".");
        });
    }

    private void onLoad() {
        YearMonth ym = selectedMonth();
        Async.run(() -> service.getPayslips(ym), slips -> {
            show(slips);
            if (slips.isEmpty()) Alerts.info("Nothing saved", "No payroll has been run for " + ym + " yet.");
        });
    }

    private void show(java.util.List<Payslip> slips) {
        data.setAll(slips);
        BigDecimal gross = slips.stream().map(Payslip::grossSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = slips.stream().map(Payslip::netSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        totals.setText(slips.size() + " payslip(s)   |   Total gross: " + Money.format(gross)
                + "   |   Total net payout: " + Money.format(net));
    }

    private void onView() {
        Payslip p = table.getSelectionModel().getSelectedItem();
        if (p != null) Alerts.showText("Payslip - " + p.employeeName(), ReportGenerator.payslipText(p));
    }

    private void onSavePayslip() {
        Payslip p = table.getSelectionModel().getSelectedItem();
        if (p == null) return;
        File file = chooseFile("payslip_" + p.empCode() + "_" + p.month() + ".txt", "Text files", "*.txt");
        if (file == null) return;
        try {
            ReportGenerator.writeText(ReportGenerator.payslipText(p), file.toPath());
            Alerts.info("Saved", "Payslip saved to\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Alerts.error("Could not save file", ex.getMessage());
        }
    }

    private void onExport() {
        if (data.isEmpty()) {
            Alerts.info("Nothing to export", "Run or load a month's payroll first.");
            return;
        }
        File file = chooseFile("payroll_" + data.get(0).month() + ".csv", "CSV files", "*.csv");
        if (file == null) return;
        try {
            ReportGenerator.exportPayslipsCsv(data, file.toPath());
            Alerts.info("Exported", data.size() + " rows written to\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Alerts.error("Could not export", ex.getMessage());
        }
    }

    private File chooseFile(String initialName, String description, String pattern) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(initialName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, pattern));
        return fc.showSaveDialog(getTabPane().getScene().getWindow());
    }
}
