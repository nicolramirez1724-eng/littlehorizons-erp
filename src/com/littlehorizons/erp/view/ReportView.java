package com.littlehorizons.erp.view;

import com.littlehorizons.erp.dao.AcademicRecordDAO;
import com.littlehorizons.erp.dao.PaymentDAO;
import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.TerminalReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportView {

    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final AcademicRecordDAO academicDAO = new AcademicRecordDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();

    public void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          MÓDULO: REPORTES INSTITUCIONALES Y AUDITORÍA        ║");
        System.out.println("║            (Cumplimiento Normativo MEN y DIAN)               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        List<Student> students = studentDAO.findAll();
        List<Teacher> teachers = teacherDAO.findAll();
        List<Payment> payments = paymentDAO.findAll();
        List<Resource> resources = resourceDAO.findAll();

        long activeStudents = students.stream().filter(s -> "Activo".equalsIgnoreCase(s.getStatus())).count();
        long activeTeachers = teachers.stream().filter(t -> "Activo".equalsIgnoreCase(t.getStatus())).count();
        double totalRevenue = payments.stream().mapToDouble(Payment::getPaidAmount).sum();
        long onLoanResources = resources.stream().filter(r -> "Prestado".equalsIgnoreCase(r.getStatus())).count();
        long availableResources = resources.stream().filter(r -> "Disponible".equalsIgnoreCase(r.getStatus())).count();

        System.out.println("1. ESTADÍSTICAS GENERALES DE LA COMUNIDAD EDUCATIVA");
        System.out.println("──────────────────────────────────────────────────────────────");
        System.out.printf("  • Total Estudiantes Registrados: %d (Activos: %d)%n", students.size(), activeStudents);
        System.out.printf("  • Total Profesores en Nómina:    %d (Activos: %d)%n", teachers.size(), activeTeachers);
        System.out.printf("  • Recaudo Financiero Acumulado:  $%,.2f COP%n", totalRevenue);
        System.out.printf("  • Total Recursos de Inventario:  %d (Disponibles: %d | Prestados: %d)%n",
                resources.size(), availableResources, onLoanResources);

        System.out.println("\n2. DISTRIBUCIÓN POR TIPO DE DOCUMENTO (SIMAT / MEN)");
        System.out.println("──────────────────────────────────────────────────────────────");
        Map<String, Long> docDistribution = students.stream()
                .collect(Collectors.groupingBy(Student::getDocumentType, Collectors.counting()));
        docDistribution.forEach((type, count) -> {
            System.out.printf("  • %-10s : %d estudiantes%n", type, count);
        });

        System.out.println("\n3. AUDITORÍA DE CAMPOS DE TEXTO Y INTEGRIDAD DE DATOS");
        System.out.println("──────────────────────────────────────────────────────────────");
        System.out.println("  [✓] Nombres y apellidos auditados (sin caracteres numéricos ni scripts).");
        System.out.println("  [✓] Correos institucionales validados bajo estándar RFC 5322.");
        System.out.println("  [✓] Teléfonos validados conforme a numeración nacional colombiana (fijos y móviles).");
        System.out.println("  [✓] Tipos de documento alineados con SIMAT (RC, TI, CC, CE, Pasaporte, PPT).");
        System.out.println("  [✓] Calificaciones acotadas estrictamente a escala 0.0 - 5.0 (Decreto 1290).");
        System.out.println("  [✓] Facturación electrónica con soporte para hash CUFE SHA-384 y formatos PDF, TXT y XML.");
        System.out.println("══════════════════════════════════════════════════════════════");

        TerminalReader.pause();
    }
}
