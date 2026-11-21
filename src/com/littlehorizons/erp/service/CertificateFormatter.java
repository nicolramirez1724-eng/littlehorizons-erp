package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.CertificateData;
import com.littlehorizons.erp.model.CertificateSubjectLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CertificateFormatter {

    private static final String DOUBLE_LINE = "=".repeat(66);
    private static final String SINGLE_LINE = "-".repeat(66);
    private static final String MISSING_GRADE = "---";
    private static final int SUBJECT_WIDTH = 33;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private CertificateFormatter() {
    }

    public static String format(CertificateData data) {
        List<String> lines = new ArrayList<>();
        lines.add(DOUBLE_LINE);
        lines.add("           COLEGIO INTERNACIONAL \"LITTLE HORIZONS\"");
        lines.add("                  CERTIFICADO DE NOTAS - AÑO " + data.year());
        lines.add(DOUBLE_LINE);
        lines.add("DATOS DEL ESTUDIANTE");
        lines.add(SINGLE_LINE);
        lines.add("Nombre     : " + data.student().getFullName());
        lines.add("Documento  : " + data.student().getDocumentType() + " " + data.student().getDocumentNumber());
        lines.add("Grado      : " + data.gradeName());
        lines.add(DOUBLE_LINE);
        lines.add("                        REGISTRO DE CALIFICACIONES");
        lines.add(DOUBLE_LINE);
        lines.add(String.format("%-" + SUBJECT_WIDTH + "s%-8s%-8s%-9s%-9s%s",
                "Materia", "Per I", "Per II", "Per III", "Per IV", "Promedio"));
        lines.add(SINGLE_LINE);
        for (CertificateSubjectLine subject : data.subjects()) {
            lines.add(formatSubjectLine(subject));
        }
        lines.add(DOUBLE_LINE);
        lines.add("PROMEDIO GENERAL DEL AÑO: " + formatGrade(data.generalAverage()));
        lines.add(DOUBLE_LINE);
        lines.add("Fecha de emisión: " + data.issueDate().format(DATE_FORMAT));
        lines.add("___________________________");
        lines.add("Secretaría Académica");
        lines.add("Colegio Internacional \"Little Horizons\"");
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    private static String formatSubjectLine(CertificateSubjectLine subject) {
        String name = subject.subjectName();
        if (name.length() > SUBJECT_WIDTH - 1) {
            name = name.substring(0, SUBJECT_WIDTH - 1);
        }
        return String.format("%-" + SUBJECT_WIDTH + "s%-8s%-8s%-9s%-9s%s",
                name,
                periodValue(subject, "I"),
                periodValue(subject, "II"),
                periodValue(subject, "III"),
                periodValue(subject, "IV"),
                formatGrade(subject.average()));
    }

    private static String periodValue(CertificateSubjectLine subject, String period) {
        BigDecimal value = subject.gradesByPeriod().get(period);
        return value == null ? MISSING_GRADE : formatGrade(value);
    }

    private static String formatGrade(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }
}
