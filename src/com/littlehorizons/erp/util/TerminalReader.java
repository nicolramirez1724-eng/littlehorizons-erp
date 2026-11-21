package com.littlehorizons.erp.util;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Robust console input reader designed to avoid crashes (e.g. InputMismatchException),
 * sanitize user text, and enforce validation rules interactively.
 */
public class TerminalReader {

    private static final Scanner SCANNER = new Scanner(System.in);

    private TerminalReader() {
        // Utility class
    }

    /**
     * Reads a required non-empty string.
     */
    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = SCANNER.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("  [!] Este campo no puede estar vacío. Intente de nuevo.");
        }
    }

    /**
     * Reads an optional string (can be empty).
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt + " (Opcional, presione Enter para omitir): ");
        String input = SCANNER.nextLine();
        return (input == null || input.trim().isEmpty()) ? null : input.trim();
    }

    /**
     * Reads a string that must satisfy a given predicate and error message.
     */
    public static String readValidatedString(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = SCANNER.nextLine();
            if (input != null && validator.test(input.trim())) {
                return input.trim();
            }
            System.out.println("  [!] " + errorMessage);
        }
    }

    /**
     * Reads an integer within [min, max] range.
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " [" + min + " - " + max + "]: ");
            String line = SCANNER.nextLine();
            if (line == null) continue;
            try {
                int value = Integer.parseInt(line.trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  [!] El valor debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Entrada inválida. Ingrese un número entero.");
            }
        }
    }

    /**
     * Reads a double within [min, max] range.
     */
    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.printf("%s [%.1f - %.1f]: ", prompt, min, max);
            String line = SCANNER.nextLine();
            if (line == null) continue;
            try {
                // Support comma or period
                String clean = line.trim().replace(',', '.');
                double value = Double.parseDouble(clean);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] El valor debe estar entre %.1f y %.1f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Entrada inválida. Ingrese un valor numérico decimal válido.");
            }
        }
    }

    /**
     * Reads a strict date (YYYY-MM-DD).
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String line = SCANNER.nextLine();
            if (line != null && InputValidator.isValidDate(line.trim())) {
                return InputValidator.parseDate(line.trim());
            }
            System.out.println("  [!] Fecha inválida o inexistente. Use el formato YYYY-MM-DD (Ej: 2026-03-15).");
        }
    }

    /**
     * Reads a confirmation (Y/N or S/N).
     */
    public static boolean readConfirmation(String prompt) {
        System.out.print(prompt + " (S/N): ");
        String line = SCANNER.nextLine();
        if (line == null) return false;
        String trimmed = line.trim().toUpperCase();
        return trimmed.equals("S") || trimmed.equals("SI") || trimmed.equals("Y") || trimmed.equals("YES");
    }

    /**
     * Reads a raw trimmed line without validation.
     */
    public static String readLine(String prompt) {
        System.out.print(prompt + " ");
        String line = SCANNER.nextLine();
        return line == null ? "" : line.trim();
    }

    /**
     * Shows a custom message and pauses until user presses Enter.
     */
    public static void pause(String message) {
        System.out.println(message);
        SCANNER.nextLine();
    }

    /**
     * Pauses until user presses Enter.
     */
    public static void pause() {
        System.out.println();
        System.out.print("Presione Enter para continuar...");
        SCANNER.nextLine();
    }
}
