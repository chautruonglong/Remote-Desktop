package com.gui.common;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

/**
 * Shared GUI infrastructure for background work, EDT handoff, and user-facing errors.
 *
 * <p>This centralizes common Swing patterns so panels can focus on business logic instead
 * of repeating thread setup, dialog wiring, and event-dispatch checks.</p>
 */
public final class UiSupport {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    private UiSupport() {}

    /**
     * Starts a named daemon platform thread and returns it to the caller.
     *
     * @param threadName the thread name
     * @param action the code to run
     * @return the started thread
     */
    public static Thread startDaemon(String threadName, Runnable action) {
        Thread thread = Thread.ofPlatform().daemon().name(threadName).unstarted(action);
        thread.start();
        return thread;
    }

    /**
     * Runs the action on the EDT immediately if already on it, otherwise schedules it.
     *
     * @param action the UI action to run
     */
    public static void runOnEdt(Runnable action) {
        if (EventQueue.isDispatchThread()) {
            action.run();
        } else {
            EventQueue.invokeLater(action);
        }
    }

    /**
     * Shows an error dialog on the EDT.
     *
     * @param parent the dialog parent component
     * @param context the user-facing action description
     * @param exception the exception to report
     */
    public static void showError(Component parent, String context, Exception exception) {
        runOnEdt(() -> JOptionPane.showMessageDialog(
            parent,
            context + ":\n" + exception.getMessage()
        ));
    }

    /**
     * Logs an error message consistently to stderr.
     *
     * @param context the error context
     * @param exception the exception to log
     */
    public static void logError(String context, Exception exception) {
        System.err.println(context + ": " + exception.getMessage());
    }

    /**
     * Runs a background task and routes completion callbacks back onto the EDT.
     *
     * @param threadName the daemon thread name
     * @param task the background work
     * @param onError invoked on the EDT if the task throws
     * @param onFinally always invoked on the EDT after completion
     * @return the started background thread
     */
    public static Thread runAsync(
        String threadName,
        ThrowingRunnable task,
        Consumer<? super Exception> onError,
        Runnable onFinally
    ) {
        return startDaemon(threadName, () -> {
            try {
                task.run();
            } catch (Exception exception) {
                if (onError != null) {
                    runOnEdt(() -> onError.accept(exception));
                }
            } finally {
                if (onFinally != null) {
                    runOnEdt(onFinally);
                }
            }
        });
    }
}
