package com.payroll.ui;

import com.payroll.model.Employee;
import com.payroll.report.ReportGenerator;
import com.payroll.service.PayrollService;
import com.payroll.util.Money;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.function.Function;

public class EmployeeTab extends Tab {
    private final PayrollService service;
    private final ObservableList<Employee> data = FXCollections.observableArrayList();
    private final TableView<Employee> table = new TableView<>(data);
    private final TextField search = new TextField();
    private final Label count = new Label();

    public EmployeeTab(PayrollService service) {
        super("Employees");
        this.service = service;
        buildTable();

        search.setPromptText("Search code, name, department, designation…");
        search.setPrefWidth(320);
        search.setOnAction(e -> refresh());

        Button searchBtn = new Button("Search");
        Button add = new Button("Add Employee");
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");
        Button salary = new Button("Salary Breakdown");
        Button reload = new Button("Refresh");
        add.getStyleClass().add("primary");
        delete.getStyleClass().add("danger");

        searchBtn.setOnAction(e -> refresh());
        reload.setOnAction(e -> { search.clear(); refresh(); });
        add.setOnAction(e -> onAdd());
        edit.setOnAction(e -> onEdit());
        delete.setOnAction(e -> onDelete());
        salary.setOnAction(e -> onSalary());

        // Buttons that need a selected row stay disabled until one is selected
        var noSelection = table.getSelectionModel().selectedItemProperty().isNull();
        edit.disableProperty().bind(noSelection);
        delete.disableProperty().bind(noSelection);
        salary.disableProperty().bind(noSelection);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8, search, searchBtn, reload, spacer, add, edit, delete, salary);

        VBox root = new VBox(10, toolbar, table, count);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setPadding(new Insets(12));
        setContent(root);
        refresh();
    }

    private void buildTable() {
        table.getColumns().add(col("Code", Employee::getEmpCode, 80));
        table.getColumns().add(col("Name", Employee::getName, 160));
        table.getColumns().add(col("Category", e -> e.getType().toString(), 90));
        table.getColumns().add(col("Department", Employee::getDepartment, 120));
        table.getColumns().add(col("Designation", Employee::getDesignation, 160));
        table.getColumns().add(col("Joined", e -> e.getJoinDate().toString(), 95));
        table.getColumns().add(col("Gross / month", e -> Money.format(e.calculateGrossSalary()), 120));
        table.getColumns().add(col("Net / month", e -> Money.format(e.calculateNetSalary()), 120));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No employees found. Click 'Add Employee' to create one."));
        table.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) onEdit();
            });
            return row;
        });
    }

    private static TableColumn<Employee, String> col(String title, Function<Employee, String> getter, double width) {
        TableColumn<Employee, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(getter.apply(cd.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    public void refresh() {
        String keyword = search.getText();
        Async.run(() -> service.searchEmployees(keyword), list -> {
            data.setAll(list);
            count.setText(list.size() + " employee(s)");
        });
    }

    private void onAdd() {
        new EmployeeFormDialog(null).showAndWait().ifPresent(emp ->
                Async.run(() -> { service.addEmployee(emp); return emp; }, saved -> {
                    refresh();
                    Alerts.info("Employee added", saved.getName() + " was saved as " + saved.getEmpCode() + ".");
                }));
    }

    private void onEdit() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        new EmployeeFormDialog(selected).showAndWait().ifPresent(emp ->
                Async.run(() -> { service.updateEmployee(emp); return emp; }, saved -> refresh()));
    }

    private void onDelete() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (Alerts.confirm("Delete " + selected.getName() + "?",
                "This also deletes all of their payslips. This cannot be undone.")) {
            Async.run(() -> { service.deleteEmployee(selected.getId()); return selected; }, gone -> refresh());
        }
    }

    private void onSalary() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alerts.showText("Salary breakdown - " + selected.getName(),
                ReportGenerator.payslipText(selected.generatePayslip(YearMonth.now())));
    }
}
