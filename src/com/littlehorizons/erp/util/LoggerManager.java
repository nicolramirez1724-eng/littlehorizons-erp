package com.littlehorizons.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe logging utility that writes errors to error_log.txt
 * as mandated by project instructions.
 */
public class LoggerManager {

    private static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggerManager() {
        // Utility class
    }

    /**
     * Logs an error message and exception stack trace to error_log.txt.
     *
     * @param message Descriptive message
     * @param throwable Exception thrown, if any
     */
    public static synchronized void logError(String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("================================================================================");
            pw.printf("[%s] [ERROR] %s%n", timestamp, message);
            if (throwable != null) {
                pw.printf("Exception: %s - %s%n", throwable.getClass().getName(), throwable.getMessage());
                throwable.printStackTrace(pw);
            }
            pw.println("================================================================================");
        } catch (IOException e) {
            System.err.printf("[%s] Failed to write to log file %s: %s%n", timestamp, LOG_FILE, e.getMessage());
        }
    }

    /**
     * Logs an informational audit message.
     *
     * @param message Informational message
     */
    public static synchronized void logInfo(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("[%s] [INFO] %s%n", timestamp, message);
        } catch (IOException e) {
            System.err.printf("[%s] Failed to write info log: %s%n", timestamp, e.getMessage());
        }
    }
}
