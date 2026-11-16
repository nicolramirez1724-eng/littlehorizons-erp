package com.littlehorizons.erp.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Rigorous validation utility auditing all user text and numeric inputs.
 * Strictly implements Colombian standards (MEN, SIMAT, and DIAN).
 */
public class InputValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{2,60}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(3\\d{9}|60\\d{8}|\\d{7,10})$");
    private static final Pattern CC_PATTERN = Pattern.compile("^\\d{6,10}$");
    private static final Pattern TI_RC_PATTERN = Pattern.compile("^\\d{10,11}$");
    private static final Pattern CE_PATTERN = Pattern.compile("^\\d{6,9}$");
    private static final Pattern PPT_PATTERN = Pattern.compile("^\\d{7,9}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Za-z]{1,2}[A-Za-z0-9]{5,8}$");

    private static final List<String> STUDENT_DOC_TYPES = Arrays.asList("RC", "TI", "CC", "CE", "Pasaporte", "PPT");
    private static final List<String> ADULT_DOC_TYPES = Arrays.asList("CC", "CE", "Pasaporte", "PPT");
    private static final List<String> BLOOD_TYPES = Arrays.asList("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");
    private static final List<String> ACADEMIC_PERIODS = Arrays.asList("I", "II", "III", "IV", "Final");
    private static final List<String> PAYMENT_METHODS = Arrays.asList("Efectivo", "Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Cheque");
    private static final List<String> ATTENDANCE_STATUSES = Arrays.asList("Presente", "Ausente", "Tardanza", "Excusa Médica");

    private static final DateTimeFormatter STRICT_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private InputValidator() {
        // Utility class
    }

    /**
     * Validates personal names or surnames.
     */
    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates email according to standard format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates Colombian telephone numbers (mobile 3XX, landline 60X, or local 7-10 digits).
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String clean = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Validates student document type per SIMAT standard.
     */
    public static boolean isValidStudentDocType(String type) {
        return type != null && STUDENT_DOC_TYPES.contains(type.trim());
    }

    /**
     * Validates adult document type (Guardian / Teacher).
     */
    public static boolean isValidAdultDocType(String type) {
        return type != null && ADULT_DOC_TYPES.contains(type.trim());
    }

    /**
     * Validates document number according to its specific legal type.
     */
    public static boolean isValidDocumentNumber(String docType, String docNumber) {
        if (docType == null || docNumber == null) return false;
        String num = docNumber.trim();
        switch (docType.trim().toUpperCase()) {
            case "CC":
                return CC_PATTERN.matcher(num).matches();
            case "TI":
            case "RC":
                return TI_RC_PATTERN.matcher(num).matches();
            case "CE":
                return CE_PATTERN.matcher(num).matches();
            case "PPT":
                return PPT_PATTERN.matcher(num).matches();
            case "PASAPORTE":
                return PASSPORT_PATTERN.matcher(num).matches();
            default:
                return num.length() >= 5 && num.length() <= 20;
        }
    }

    /**
     * Validates strict date string format (YYYY-MM-DD).
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        try {
            LocalDate.parse(dateStr.trim(), STRICT_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Parses date string into LocalDate with strict validation.
     */
    public static LocalDate parseDate(String dateStr) {
        if (!isValidDate(dateStr)) return null;
        return LocalDate.parse(dateStr.trim(), STRICT_DATE_FORMATTER);
    }

    /**
     * Validates student birth date within realistic school age (3 to 22 years old).
     */
    public static boolean isValidStudentBirthDate(LocalDate birthDate) {
        if (birthDate == null) return false;
        LocalDate now = LocalDate.now();
        if (birthDate.isAfter(now)) return false;
        int age = Period.between(birthDate, now).getYears();
        return age >= 3 && age <= 22;
    }

    /**
     * Validates academic grade (0.0 to 5.0 per MEN standard).
     */
    public static boolean isValidGrade(double grade) {
        return grade >= 0.0 && grade <= 5.0;
    }

    /**
     * Validates monetary amounts (non-negative).
     */
    public static boolean isValidAmount(double amount) {
        return amount >= 0.0;
    }

    /**
     * Validates blood type group.
     */
    public static boolean isValidBloodType(String bloodType) {
        return bloodType != null && BLOOD_TYPES.contains(bloodType.trim().toUpperCase());
    }

    /**
     * Validates academic period identifier.
     */
    public static boolean isValidPeriod(String period) {
        return period != null && ACADEMIC_PERIODS.contains(period.trim());
    }

    /**
     * Validates payment method.
     */
    public static boolean isValidPaymentMethod(String method) {
        return method != null && PAYMENT_METHODS.contains(method.trim());
    }

    /**
     * Validates attendance status.
     */
    public static boolean isValidAttendanceStatus(String status) {
        return status != null && ATTENDANCE_STATUSES.contains(status.trim());
    }
}
