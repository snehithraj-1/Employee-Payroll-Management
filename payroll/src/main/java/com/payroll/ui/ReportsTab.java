package com.payroll.ui;

import com.payroll.report.ReportGenerator;
import com.payroll.service.PayrollService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class ReportsTab extends Tab {
    private final PayrollService service;
    private final TextArea area = new TextArea();

    public ReportsTab(PayrollService service) {
        super("Reports");
        this.service = service;

        area.setEditable(false);
        area.getStyleClass().add("mono");

        Button refresh = new Button("Refresh");
        Button export = new Button("Save Report (.txt)");
        refresh.setOnAction(e -> refresh());
        export.setOnAction(e -> onExport());

        VBox root = new VBox(10, new HBox(8, refresh, export), area);
        VBox.setVgrow(area, Priority.ALWAYS);
        root.setPadding(new Insets(12));
        setContent(root);
    }

    public void refresh() {
        Async.run(service::getAllEmployees, list -> area.setText(ReportGenerator.summaryReport(list)));
    }

    private void onExport() {
        if (area.getText().isBlank()) return;
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("payroll_summary_" + LocalDate.now() + ".txt");
        File file = fc.showSaveDialog(getTabPane().getScene().getWindow());
        if (file == null) return;
        try {
            ReportGenerator.writeText(area.getText(), file.toPath());
            Alerts.info("Saved", "Report saved to\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Alerts.error("Could not save report", ex.getMessage());
        }
    }
}
