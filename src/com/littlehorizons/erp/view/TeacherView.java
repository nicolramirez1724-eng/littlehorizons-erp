package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.InputValidator;
import com.littlehorizons.erp.util.TerminalReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TeacherView {

    private final TeacherController controller;

    public TeacherView(TeacherController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                 MÓDULO: GESTIÓN DE PROFESORES                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Listar Directorio de Profesores");
            System.out.println("  2. Registrar Nuevo Profesor");
            System.out.println("  3. Consultar Profesor por Documento");
            System.out.println("  4. Asignar Materia a Profesor (Carga Académica)");
            System.out.println("  5. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 5);
            switch (option) {
                case 1:
                    listTeachers();
                    break;
                case 2:
                    registerTeacher();
                    break;
                case 3:
                    searchTeacher();
                    break;
                case 4:
                    assignSubject();
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    private void listTeachers() {
        System.out.println("\n--- DIRECTORIO DE PROFESORES ---");
        List<Teacher> teachers = controller.listAllTeachers();
        if (teachers.isEmpty()) {
            System.out.println("No hay profesores registrados.");
        } else {
            System.out.printf("%-5s | %-12s | %-30s | %-28s | %-12s%n",
                    "ID", "DOCUMENTO", "NOMBRE COMPLETO", "ESPECIALIZACIÓN", "TELÉFONO");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (Teacher t : teachers) {
                System.out.printf("%-5d | %-12s | %-30s | %-28s | %-12s%n",
                        t.getId(), t.getDocumentNumber(), t.getFullName(), t.getSpecialization(), t.getPhone() != null ? t.getPhone() : "N/A");
            }
        }
        TerminalReader.pause();
    }

    private void registerTeacher() {
        System.out.println("\n--- REGISTRO DE NUEVO PROFESOR ---");
        String docType = TerminalReader.readValidatedString("Tipo de Documento (CC, CE, Pasaporte, PPT)",
                InputValidator::isValidAdultDocType, "Tipo inválido para profesores.");

        String docNum = TerminalReader.readValidatedString("Número de Documento",
                num -> InputValidator.isValidDocumentNumber(docType, num), "Número de documento inválido.");

        if (controller.getTeacherByDocument(docNum).isPresent()) {
            System.out.println("  [!] Ya existe un profesor con ese documento.");
            TerminalReader.pause();
            return;
        }

        String firstName = TerminalReader.readValidatedString("Primer Nombre", InputValidator::isValidName, "Nombre inválido");
        String secondName = TerminalReader.readOptionalString("Segundo Nombre");
        String firstLastName = TerminalReader.readValidatedString("Primer Apellido", InputValidator::isValidName, "Apellido inválido");
        String secondLastName = TerminalReader.readOptionalString("Segundo Apellido");
        String email = TerminalReader.readValidatedString("Correo Electrónico Institucional", InputValidator::isValidEmail, "Correo inválido");
        String phone = TerminalReader.readValidatedString("Teléfono Móvil", InputValidator::isValidPhone, "Teléfono inválido");
        String spec = TerminalReader.readNonEmptyString("Área de Especialización (Ej: Matemáticas, Lenguaje)");
        LocalDate hireDate = TerminalReader.readDate("Fecha de Contratación");

        Teacher teacher = new Teacher(0, docType, docNum, firstName, secondName, firstLastName, secondLastName,
                email, phone, spec, hireDate, "Activo");

        boolean ok = controller.registerTeacher(teacher);
        if (ok) {
            System.out.println("  [✓] Profesor registrado con éxito con ID: " + teacher.getId());
        } else {
            System.out.println("  [✗] Error al registrar profesor.");
        }
        TerminalReader.pause();
    }

    private void searchTeacher() {
        System.out.println("\n--- CONSULTAR PROFESOR ---");
        String doc = TerminalReader.readNonEmptyString("Número de documento");
        Optional<Teacher> opt = controller.getTeacherByDocument(doc);
        if (opt.isPresent()) {
            Teacher t = opt.get();
            System.out.println("════════════════════════════════════════════════════════════");
            System.out.printf("Profesor: %s (ID: %d)%n", t.getFullName(), t.getId());
            System.out.printf("Documento: %s %s | Estado: %s%n", t.getDocumentType(), t.getDocumentNumber(), t.getStatus());
            System.out.printf("Especialización: %s%n", t.getSpecialization());
            System.out.printf("Correo: %s | Teléfono: %s%n", t.getEmail(), t.getPhone());
            System.out.printf("Fecha de Contratación: %s%n", t.getHireDate());
            System.out.println("════════════════════════════════════════════════════════════");
        } else {
            System.out.println("  [!] Profesor no encontrado.");
        }
        TerminalReader.pause();
    }

    private void assignSubject() {
        System.out.println("\n--- ASIGNACIÓN DE CARGA ACADÉMICA ---");
        int teacherId = TerminalReader.readInt("ID del Profesor", 1, 100);
        int subjectId = TerminalReader.readInt("ID de la Materia", 1, 100);
        int year = TerminalReader.readInt("Año Académico", 2024, 2030);
        String section = TerminalReader.readNonEmptyString("Sección o Grupo (A, B, C)");

        boolean ok = controller.assignSubject(teacherId, subjectId, year, section.toUpperCase());
        if (ok) {
            System.out.println("  [✓] Asignación de materia registrada con éxito.");
        } else {
            System.out.println("  [✗] Error al registrar la asignación.");
        }
        TerminalReader.pause();
    }
}
