package com.payroll.ui;

import com.payroll.exception.ValidationException;
import com.payroll.model.Employee;
import com.payroll.model.EmployeeFactory;
import com.payroll.model.EmployeeType;
import com.payroll.model.PayDetails;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Add / edit form. Pay fields change depending on the selected category. */
public class EmployeeFormDialog extends Dialog<Employee> {
    private final int existingId;

    private final TextField code = new TextField();
    private final TextField name = new TextField();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final TextField department = new TextField();
    private final TextField designation = new TextField();
    private final DatePicker joinDate = new DatePicker(LocalDate.now());
    private final ComboBox<EmployeeType> type =
            new ComboBox<>(FXCollections.observableArrayList(EmployeeType.values()));

    private final TextField monthlySalary = new TextField();
    private final TextField teamSize = new TextField();
    private final TextField hourlyRate = new TextField();
    private final TextField hoursWorked = new TextField();
    private final TextField contractAmount = new TextField();
    private final TextField stipend = new TextField();

    private final GridPane payGrid = grid();
    private Employee result;

    public EmployeeFormDialog(Employee existing) {
        existingId = existing == null ? 0 : existing.getId();
        setTitle(existing == null ? "Add Employee" : "Edit Employee");
        setHeaderText(existing == null ? "Enter the new employee's details" : "Editing " + existing.getEmpCode());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        code.setPromptText("EMP006");
        phone.setPromptText("10-digit mobile");
        joinDate.setEditable(false);

        GridPane main = grid();
        addRow(main, "Employee code*", code);
        addRow(main, "Full name*", name);
        addRow(main, "Email*", email);
        addRow(main, "Phone*", phone);
        addRow(main, "Department*", department);
        addRow(main, "Designation*", designation);
        addRow(main, "Join date*", joinDate);
        addRow(main, "Category*", type);

        type.valueProperty().addListener((obs, old, now) -> showPayFields(now));

        VBox content = new VBox(10, main, new Separator(), new Label("Pay details"), payGrid);
        content.setPadding(new Insets(10));
        getDialogPane().setContent(content);

        if (existing != null) {
            fill(existing);
        } else {
            type.setValue(EmployeeType.FULL_TIME);
        }

        // Validate before the dialog closes; keep it open if anything is wrong.
        Node saveButton = getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                result = buildEmployee();
                result.validate();
            } catch (ValidationException ex) {
                Alerts.showException(ex);
                ev.consume();
            }
        });
        setResultConverter(bt -> bt == saveType ? result : null);
    }

    private void showPayFields(EmployeeType t) {
        payGrid.getChildren().clear();
        if (t == null) return;
        switch (t) {
            case FULL_TIME -> addRow(payGrid, "Monthly basic salary*", monthlySalary);
            case MANAGER -> {
                addRow(payGrid, "Monthly basic salary*", monthlySalary);
                addRow(payGrid, "Team size*", teamSize);
            }
            case PART_TIME -> {
                addRow(payGrid, "Hourly rate*", hourlyRate);
                addRow(payGrid, "Hours worked this month*", hoursWorked);
            }
            case CONTRACT -> addRow(payGrid, "Monthly contract amount*", contractAmount);
            case INTERN -> addRow(payGrid, "Monthly stipend*", stipend);
        }
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().sizeToScene();
        }
    }

    private Employee buildEmployee() throws ValidationException {
        EmployeeType t = type.getValue();
        if (t == null) {
            throw new ValidationException(List.of("Please choose a category."));
        }
        List<String> errors = new ArrayList<>();
        BigDecimal monthly = null, rate = null, hours = null, contract = null, stip = null;
        Integer team = null;
        switch (t) {
            case FULL_TIME -> monthly = parseAmount("Monthly salary", monthlySalary, errors);
            case MANAGER -> {
                monthly = parseAmount("Monthly salary", monthlySalary, errors);
                team = parseInt("Team size", teamSize, errors);
            }
            case PART_TIME -> {
                rate = parseAmount("Hourly rate", hourlyRate, errors);
                hours = parseAmount("Hours worked", hoursWorked, errors);
            }
            case CONTRACT -> contract = parseAmount("Contract amount", contractAmount, errors);
            case INTERN -> stip = parseAmount("Stipend", stipend, errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return EmployeeFactory.create(t, existingId, code.getText(), name.getText(), email.getText(),
                phone.getText(), department.getText(), designation.getText(), joinDate.getValue(),
                new PayDetails(monthly, rate, hours, contract, stip, team));
    }

    private void fill(Employee e) {
        code.setText(e.getEmpCode());
        name.setText(e.getName());
        email.setText(e.getEmail());
        phone.setText(e.getPhone());
        department.setText(e.getDepartment());
        designation.setText(e.getDesignation());
        joinDate.setValue(e.getJoinDate());
        PayDetails p = PayDetails.from(e);
        monthlySalary.setText(text(p.monthlySalary()));
        hourlyRate.setText(text(p.hourlyRate()));
        hoursWorked.setText(text(p.hoursWorked()));
        contractAmount.setText(text(p.contractAmount()));
        stipend.setText(text(p.stipend()));
        teamSize.setText(p.teamSize() == null ? "" : p.teamSize().toString());
        type.setValue(e.getType());
    }

    // ---- small helpers ----

    private static BigDecimal parseAmount(String label, TextField field, List<String> errors) {
        String s = field.getText() == null ? "" : field.getText().trim().replace(",", "");
        if (s.isEmpty()) {
            errors.add(label + " is required.");
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            errors.add(label + " must be a number.");
            return null;
        }
    }

    private static Integer parseInt(String label, TextField field, List<String> errors) {
        String s = field.getText() == null ? "" : field.getText().trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            errors.add(label + " must be a whole number.");
            return null;
        }
    }

    private static String text(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(8);
        return g;
    }

    private static void addRow(GridPane g, String label, Node field) {
        if (field instanceof TextField tf) tf.setPrefColumnCount(22);
        g.addRow(g.getRowCount(), new Label(label), field);
    }
}
