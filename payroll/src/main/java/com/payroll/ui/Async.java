package com.payroll.ui;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Runs database work off the JavaFX thread so the window never freezes.
 * A single worker thread keeps DB operations in order.
 */
public final class Async {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "db-worker");
        t.setDaemon(true);
        return t;
    });

    @FunctionalInterface
    public interface Work<T> {
        T call() throws Exception;
    }

    private Async() {
    }

    public static <T> void run(Work<T> work, Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));      // back on FX thread
        task.setOnFailed(e -> Alerts.showException(task.getException()));
        WORKER.submit(task);
    }
}
