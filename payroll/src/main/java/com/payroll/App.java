package com.payroll;

import com.payroll.dao.DBConnection;
import com.payroll.service.PayrollService;
import com.payroll.ui.Alerts;
import com.payroll.ui.EmployeeTab;
import com.payroll.ui.PayrollTab;
import com.payroll.ui.ReportsTab;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        if (!DBConnection.testConnection()) {
            Alerts.error("Cannot connect to MySQL",
                    "1. Make sure the MySQL server is running.\n"
                    + "2. Run database/schema.sql once.\n"
                    + "3. Check user/password in src/main/resources/db.properties.\n\n"
                    + "Details: " + DBConnection.getLastError());
            Platform.exit();
            return;
        }

        PayrollService service = new PayrollService();
        TabPane tabs = new TabPane(new EmployeeTab(service), new PayrollTab(service), new ReportsTab(service));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if (now instanceof ReportsTab r) r.refresh();     // always show fresh numbers
        });

        Label title = new Label("Employee Payroll Management System");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("JavaFX • JDBC • MySQL");
        subtitle.getStyleClass().add("app-subtitle");
        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("app-header");

        BorderPane root = new BorderPane(tabs);
        root.setTop(header);

        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(App.class.getResource("/styles.css").toExternalForm());
        stage.setTitle("Payroll Management");
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
