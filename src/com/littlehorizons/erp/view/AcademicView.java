package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.InputValidator;
import com.littlehorizons.erp.util.TerminalReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AcademicView {

    private final AcademicController controller;

    public AcademicView(AcademicController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                 MÓDULO: GESTIÓN ACADÉMICA                    ║");
            System.out.println("║      (Marco Legal: Ministerio de Educación - Dec. 1290)      ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Registrar Calificación a Estudiante (0.0 a 5.0)");
            System.out.println("  2. Consultar Boletín / Historial de Notas (Escala MEN)");
            System.out.println("  3. Consultar Promedio General del Curso por Materia");
            System.out.println("  4. Registro Diario de Asistencia");
            System.out.println("  5. Reporte de Inasistencias y Alertas de Ausentismo");
            System.out.println("  6. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 6);
            switch (option) {
                case 1:
                    registerGrade();
                    break;
                case 2:
                    viewReportCard();
                    break;
                case 3:
                    viewCourseAverage();
                    break;
                case 4:
                    recordAttendance();
                    break;
                case 5:
                    viewAttendanceReport();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private void registerGrade() {
        System.out.println("\n--- REGISTRO DE CALIFICACIÓN (DECRETO 1290 DE 2009) ---");
        String doc = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.findStudentByDocument(doc);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();
        System.out.println("Estudiante: " + student.getFullName());

        List<Subject> subjects = controller.getAllSubjects();
        System.out.println("\nMaterias registradas:");
        for (int i = 0; i < Math.min(subjects.size(), 15); i++) {
            Subject s = subjects.get(i);
            System.out.printf("  [%d] %s (%s)%n", s.getId(), s.getName(), s.getCode());
        }

        int subjectId = TerminalReader.readInt("ID de la Materia", 1, 50);
        int teacherId = TerminalReader.readInt("ID del Profesor que califica", 1, 30);
        int year = TerminalReader.readInt("Año Académico", 2024, 2030);

        String period = TerminalReader.readValidatedString(
                "Período Académico (I, II, III, IV, Final)",
                InputValidator::isValidPeriod,
                "Período inválido. Opciones permitidas: I, II, III, IV, Final"
        );

        double grade = TerminalReader.readDouble("Calificación Numérica (0.0 a 5.0)", 0.0, 5.0);
        String obs = TerminalReader.readOptionalString("Observaciones pedagógicas");

        boolean ok = controller.registerGrade(student.getId(), subjectId, teacherId, year, period, grade, obs);
        if (ok) {
            String concept = controller.getMenQualitativeConcept(grade);
            System.out.printf("  [✓] Calificación registrada con éxito: %.1f -> %s%n", grade, concept);
        } else {
            System.out.println("  [✗] Error al registrar la calificación.");
        }
        TerminalReader.pause();
    }

    private void viewReportCard() {
        System.out.println("\n--- BOLETÍN E HISTORIAL ACADÉMICO (ESCALA NACIONAL MEN) ---");
        String doc = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.findStudentByDocument(doc);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();

        Map<Integer, List<AcademicRecord>> history = controller.getStudentAcademicHistory(student.getId());
        if (history.isEmpty()) {
            System.out.println("  [!] El estudiante no registra calificaciones en el sistema.");
        } else {
            System.out.println("════════════════════════════════════════════════════════════════════════════════════");
            System.out.printf("BOLETÍN OFICIAL DE VALORACIÓN: %s%n", student.getFullName().toUpperCase());
            System.out.printf("Documento: %s %s | Estado: %s%n", student.getDocumentType(), student.getDocumentNumber(), student.getStatus());
            System.out.println("Escala Nacional (Decreto 1290): Superior (4.6-5.0), Alto (4.0-4.5), Básico (3.0-3.9), Bajo (0.0-2.9)");
            System.out.println("════════════════════════════════════════════════════════════════════════════════════");

            history.forEach((year, records) -> {
                System.out.printf("%n>>> AÑO ACADÉMICO: %d <<<%n", year);
                System.out.printf("%-6s | %-32s | %-5s | %-24s | %-20s%n",
                        "PER", "ASIGNATURA", "NOTA", "DESEMPEÑO MEN", "OBSERVACIONES");
                System.out.println("------------------------------------------------------------------------------------");
                for (AcademicRecord r : records) {
                    System.out.printf("%-6s | %-32s | %-5.1f | %-24s | %-20s%n",
                            r.getAcademicPeriod(),
                            r.getSubjectName() != null ? r.getSubjectName() : "Materia " + r.getSubjectId(),
                            r.getGradeValue(),
                            r.getMenPerformanceLevel(),
                            r.getObservations() != null ? r.getObservations() : "");
                }

                // Calculate period average
                double avgPeriodI = controller.getStudentPeriodAverage(student.getId(), year, "I");
                if (avgPeriodI > 0) {
                    System.out.printf("%n  [*] Promedio Período I: %.2f [%s]%n", avgPeriodI, controller.getMenQualitativeConcept(avgPeriodI));
                }
            });
        }
        TerminalReader.pause();
    }

    private void viewCourseAverage() {
        System.out.println("\n--- PROMEDIO GENERAL DEL CURSO POR ASIGNATURA ---");
        int subjectId = TerminalReader.readInt("ID de la Materia a consultar", 1, 50);
        int year = TerminalReader.readInt("Año Académico", 2024, 2030);
        String period = TerminalReader.readValidatedString("Período (I, II, III, IV)", InputValidator::isValidPeriod, "Período inválido");

        double avg = controller.getCourseAverage(subjectId, year, period);
        System.out.println("────────────────────────────────────────────────────────────────");
        System.out.printf("Promedio general del curso en la materia ID %d: %.2f%n", subjectId, avg);
        System.out.printf("Equivalencia Institucional MEN: %s%n", controller.getMenQualitativeConcept(avg));
        System.out.println("────────────────────────────────────────────────────────────────");
        TerminalReader.pause();
    }

    private void recordAttendance() {
        System.out.println("\n--- REGISTRO DIARIO DE ASISTENCIA ---");
        String doc = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.findStudentByDocument(doc);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();

        int subjectId = TerminalReader.readInt("ID de la Materia", 1, 50);
        LocalDate date = TerminalReader.readDate("Fecha de Asistencia");

        System.out.println("Estados de asistencia:");
        System.out.println(" 1. Presente  |  2. Ausente  |  3. Tardanza  |  4. Excusa Médica");
        int stOpt = TerminalReader.readInt("Seleccione", 1, 4);
        String[] statuses = {"Presente", "Ausente", "Tardanza", "Excusa Médica"};
        String status = statuses[stOpt - 1];

        String obs = TerminalReader.readOptionalString("Observación o Justificación");
        int teacherId = TerminalReader.readInt("ID del Profesor que registra", 1, 30);

        boolean ok = controller.recordAttendance(student.getId(), subjectId, date, status, obs, teacherId);
        if (ok) {
            System.out.println("  [✓] Registro de asistencia guardado exitosamente.");
            // Check for chronic alert
            if (controller.checkChronicAbsences(student.getId(), date)) {
                System.out.println("  [ALERTA MEN] El estudiante presenta 3 o más inasistencias en la semana.");
                System.out.println("  Se recomienda remitir el caso a Coordinación de Convivencia y Bienestar.");
            }
        } else {
            System.out.println("  [✗] Error al registrar asistencia.");
        }
        TerminalReader.pause();
    }

    private void viewAttendanceReport() {
        System.out.println("\n--- HISTORIAL DE ASISTENCIA Y CONTROL DE INASISTENCIAS ---");
        String doc = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.findStudentByDocument(doc);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();

        List<Attendance> attendances = controller.getStudentAttendanceHistory(student.getId());
        if (attendances.isEmpty()) {
            System.out.println("  (Sin registros de asistencia para este estudiante)");
        } else {
            System.out.printf("%-12s | %-28s | %-15s | %-25s%n", "FECHA", "MATERIA", "ESTADO", "OBSERVACIÓN");
            System.out.println("-----------------------------------------------------------------------------------");
            for (Attendance a : attendances) {
                System.out.printf("%-12s | %-28s | %-15s | %-25s%n",
                        a.getAttendanceDate(),
                        a.getSubjectName() != null ? a.getSubjectName() : "Materia " + a.getSubjectId(),
                        a.getStatus(),
                        a.getObservations() != null ? a.getObservations() : "");
            }
        }
        TerminalReader.pause();
    }
}
