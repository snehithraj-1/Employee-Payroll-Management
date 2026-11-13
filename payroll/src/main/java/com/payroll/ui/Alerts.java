package com.payroll.ui;

import com.payroll.exception.PayrollException;
import com.payroll.exception.ValidationException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.util.Optional;

/** Small helpers for dialogs so every screen reports errors the same way. */
public final class Alerts {
    private Alerts() {
    }

    public static void info(String header, String content) {
        show(Alert.AlertType.INFORMATION, "Information", header, content);
    }

    public static void error(String header, String content) {
        show(Alert.AlertType.ERROR, "Error", header, content);
    }

    public static boolean confirm(String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        a.setTitle("Please confirm");
        a.setHeaderText(header);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.YES;
    }

    public static void showException(Throwable t) {
        if (t instanceof ValidationException v) {
            error("Please fix the following", "• " + String.join("\n• ", v.getErrors()));
        } else if (t instanceof PayrollException p) {
            String cause = p.getCause() == null ? "" : "\n\nDetails: " + p.getCause().getMessage();
            error(p.getMessage(), "The operation could not be completed." + cause);
        } else {
            error("Unexpected error", String.valueOf(t));
        }
    }

    /** Shows read-only monospaced text (payslips, reports). */
    public static void showText(String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.getStyleClass().add("mono");
        area.setPrefSize(560, 480);
        Alert a = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
        a.setTitle(title);
        a.getDialogPane().setContent(area);
        a.setResizable(true);
        a.showAndWait();
    }

    private static void show(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
