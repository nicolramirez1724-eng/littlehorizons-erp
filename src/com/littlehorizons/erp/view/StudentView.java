package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.InputValidator;
import com.littlehorizons.erp.util.TerminalReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentView {

    private final StudentController controller;

    public StudentView(StudentController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                MÓDULO: GESTIÓN DE ESTUDIANTES                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Listar Directorio de Estudiantes");
            System.out.println("  2. Registrar Nuevo Estudiante (Validación SIMAT/MEN)");
            System.out.println("  3. Consultar Detalle de Estudiante por Documento");
            System.out.println("  4. Asociar Acudiente a Estudiante");
            System.out.println("  5. Matricular Estudiante en un Grado Escolar");
            System.out.println("  6. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 6);
            switch (option) {
                case 1:
                    listStudents();
                    break;
                case 2:
                    registerStudent();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    linkGuardian();
                    break;
                case 5:
                    enrollStudent();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private void listStudents() {
        System.out.println("\n--- DIRECTORIO GENERAL DE ESTUDIANTES ---");
        List<Student> students = controller.listAllStudents();
        if (students.isEmpty()) {
            System.out.println("No hay estudiantes registrados en el sistema.");
        } else {
            System.out.printf("%-5s | %-6s | %-12s | %-32s | %-10s | %-10s%n",
                    "ID", "TIPO", "DOCUMENTO", "NOMBRE COMPLETO", "ESTADO", "TELÉFONO");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (Student s : students) {
                System.out.printf("%-5d | %-6s | %-12s | %-32s | %-10s | %-10s%n",
                        s.getId(), s.getDocumentType(), s.getDocumentNumber(),
                        s.getFullName(), s.getStatus(), s.getPhone() != null ? s.getPhone() : "N/A");
            }
        }
        TerminalReader.pause();
    }

    private void registerStudent() {
        System.out.println("\n--- REGISTRO DE NUEVO ESTUDIANTE (MARCO SIMAT / MEN) ---");

        String docType = TerminalReader.readValidatedString(
                "Tipo de Documento (RC, TI, CC, CE, Pasaporte, PPT)",
                InputValidator::isValidStudentDocType,
                "Tipo de documento no válido según SIMAT. Opciones: RC, TI, CC, CE, Pasaporte, PPT"
        );

        String docNumber = TerminalReader.readValidatedString(
                "Número de Documento",
                num -> InputValidator.isValidDocumentNumber(docType, num),
                "Número de documento no cumple con el formato oficial para " + docType
        );

        // Check uniqueness
        if (controller.getStudentByDocument(docNumber).isPresent()) {
            System.out.println("  [!] Ya existe un estudiante registrado con el documento: " + docNumber);
            TerminalReader.pause();
            return;
        }

        String firstName = TerminalReader.readValidatedString(
                "Primer Nombre",
                InputValidator::isValidName,
                "Nombre inválido. Solo letras y longitud mínima de 2 caracteres."
        );

        String secondName = TerminalReader.readOptionalString("Segundo Nombre");

        String firstLastName = TerminalReader.readValidatedString(
                "Primer Apellido",
                InputValidator::isValidName,
                "Apellido inválido. Solo letras y longitud mínima de 2 caracteres."
        );

        String secondLastName = TerminalReader.readOptionalString("Segundo Apellido");

        LocalDate birthDate;
        while (true) {
            birthDate = TerminalReader.readDate("Fecha de Nacimiento");
            if (InputValidator.isValidStudentBirthDate(birthDate)) {
                break;
            }
            System.out.println("  [!] La edad del estudiante debe estar entre 3 y 22 años.");
        }

        String gender = TerminalReader.readValidatedString(
                "Género (M, F, Otro)",
                g -> g.equalsIgnoreCase("M") || g.equalsIgnoreCase("F") || g.equalsIgnoreCase("Otro"),
                "Opciones válidas: M, F, Otro"
        );

        String address = TerminalReader.readNonEmptyString("Dirección Residencial");

        String phone = TerminalReader.readValidatedString(
                "Teléfono de Contacto (10 dígitos celular o 7-10 fijo)",
                InputValidator::isValidPhone,
                "Número telefónico inválido según estándar colombiano (Ej: 3101234567 o 6017458900)."
        );

        String email = TerminalReader.readValidatedString(
                "Correo Electrónico Institucional o Personal",
                InputValidator::isValidEmail,
                "Formato de correo electrónico inválido (Ej: usuario@dominio.com)."
        );

        String bloodType = TerminalReader.readValidatedString(
                "Tipo de Sangre y Factor RH (O+, O-, A+, A-, B+, B-, AB+, AB-)",
                InputValidator::isValidBloodType,
                "Tipo de sangre no válido (Ej: O+, A+, B-)."
        );

        String allergies = TerminalReader.readOptionalString("Alergias Conocidas (o 'Ninguna')");
        String conditions = TerminalReader.readOptionalString("Condiciones Médicas de Atención");

        Student student = new Student(0, docType, docNumber, firstName, secondName, firstLastName, secondLastName,
                birthDate, gender.toUpperCase(), address, phone, email, bloodType.toUpperCase(),
                allergies != null ? allergies : "Ninguna", conditions != null ? conditions : "Ninguna",
                LocalDate.now(), "Activo");

        boolean success = controller.registerStudent(student);
        if (success) {
            System.out.println("\n  [✓] Estudiante registrado exitosamente en el sistema con ID: " + student.getId());
        } else {
            System.out.println("\n  [✗] Ocurrió un error al registrar el estudiante en la base de datos.");
        }
        TerminalReader.pause();
    }

    private void searchStudent() {
        System.out.println("\n--- CONSULTAR ESTUDIANTE POR DOCUMENTO ---");
        String docNumber = TerminalReader.readNonEmptyString("Ingrese el número de documento");
        Optional<Student> studentOpt = controller.getStudentByDocument(docNumber);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            System.out.println("\n════════════════════════════════════════════════════════════");
            System.out.printf("Estudiante: %s (ID: %d)%n", s.getFullName(), s.getId());
            System.out.printf("Documento: %s %s | Fecha Nacimiento: %s%n", s.getDocumentType(), s.getDocumentNumber(), s.getBirthDate());
            System.out.printf("Género: %s | Grupo Sanguíneo: %s | Estado: %s%n", s.getGender(), s.getBloodType(), s.getStatus());
            System.out.printf("Dirección: %s | Teléfono: %s%n", s.getAddress(), s.getPhone());
            System.out.printf("Correo: %s%n", s.getEmail());
            System.out.printf("Alergias: %s | Condiciones: %s%n", s.getAllergies(), s.getMedicalConditions());
            System.out.println("────────────────────────────────────────────────────────────");

            List<Guardian> guardians = controller.getGuardiansByStudent(s.getId());
            System.out.println("Acudientes Asociados:");
            if (guardians.isEmpty()) {
                System.out.println("  (Sin acudientes vinculados)");
            } else {
                for (Guardian g : guardians) {
                    System.out.printf("  - %s (%s) | Tel: %s | Financiero: %s%n",
                            g.getFullName(), g.getRelationship(), g.getMainPhone(), g.isFinancialResponsible() ? "SÍ" : "NO");
                }
            }
            System.out.println("════════════════════════════════════════════════════════════");
        } else {
            System.out.println("  [!] No se encontró ningún estudiante con el documento: " + docNumber);
        }
        TerminalReader.pause();
    }

    private void linkGuardian() {
        System.out.println("\n--- VINCULAR ACUDIENTE A ESTUDIANTE ---");
        String docNumber = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.getStudentByDocument(docNumber);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();
        System.out.println("Estudiante seleccionado: " + student.getFullName());

        System.out.println("\n¿El acudiente ya está registrado o desea registrar uno nuevo?");
        System.out.println("1. Registrar nuevo acudiente y vincularlo");
        System.out.println("2. Cancelar");
        int sub = TerminalReader.readInt("Seleccione", 1, 2);
        if (sub == 2) return;

        String gDocType = TerminalReader.readValidatedString(
                "Tipo de Documento del Acudiente (CC, CE, Pasaporte, PPT)",
                InputValidator::isValidAdultDocType,
                "Tipo no válido para adultos (CC, CE, Pasaporte, PPT)."
        );

        String gDocNum = TerminalReader.readValidatedString(
                "Número de Documento del Acudiente",
                num -> InputValidator.isValidDocumentNumber(gDocType, num),
                "Número de documento inválido para " + gDocType
        );

        String firstName = TerminalReader.readValidatedString("Primer Nombre", InputValidator::isValidName, "Nombre inválido");
        String secondName = TerminalReader.readOptionalString("Segundo Nombre");
        String firstLastName = TerminalReader.readValidatedString("Primer Apellido", InputValidator::isValidName, "Apellido inválido");
        String secondLastName = TerminalReader.readOptionalString("Segundo Apellido");
        String relationship = TerminalReader.readNonEmptyString("Parentesco (Padre, Madre, Tutor Legal, etc.)");
        String mainPhone = TerminalReader.readValidatedString("Teléfono Principal", InputValidator::isValidPhone, "Teléfono inválido");
        String secPhone = TerminalReader.readOptionalString("Teléfono Secundario");
        String email = TerminalReader.readValidatedString("Correo Electrónico", InputValidator::isValidEmail, "Correo inválido");
        String address = TerminalReader.readNonEmptyString("Dirección Residencial");
        String occupation = TerminalReader.readOptionalString("Ocupación o Profesión");
        boolean isFinancial = TerminalReader.readConfirmation("¿Es el responsable financiero de los pagos?");

        Guardian guardian = new Guardian(0, gDocType, gDocNum, firstName, secondName, firstLastName, secondLastName,
                relationship, mainPhone, secPhone, email, address, occupation != null ? occupation : "No especificada", isFinancial);

        boolean savedG = controller.registerGuardian(guardian);
        if (savedG) {
            boolean linked = controller.linkStudentGuardian(student.getId(), guardian.getId(), true, true);
            if (linked) {
                System.out.println("  [✓] Acudiente registrado y vinculado exitosamente al estudiante.");
            } else {
                System.out.println("  [!] Acudiente registrado pero no se pudo completar el vínculo.");
            }
        } else {
            System.out.println("  [✗] Error al registrar el acudiente.");
        }
        TerminalReader.pause();
    }

    private void enrollStudent() {
        System.out.println("\n--- MATRICULAR ESTUDIANTE EN GRADO ESCOLAR ---");
        String docNumber = TerminalReader.readNonEmptyString("Número de documento del estudiante");
        Optional<Student> studentOpt = controller.getStudentByDocument(docNumber);
        if (!studentOpt.isPresent()) {
            System.out.println("  [!] Estudiante no encontrado.");
            TerminalReader.pause();
            return;
        }
        Student student = studentOpt.get();
        System.out.println("Estudiante: " + student.getFullName());

        System.out.println("Grados disponibles:");
        System.out.println(" 1. Preescolar  |  2. Primero    |  3. Segundo    |  4. Tercero");
        System.out.println(" 5. Cuarto      |  6. Quinto     |  7. Sexto      |  8. Séptimo");
        System.out.println(" 9. Octavo      | 10. Noveno     | 11. Décimo     | 12. Once");

        int gradeId = TerminalReader.readInt("Seleccione el Grado (1 - 12)", 1, 12);
        int year = TerminalReader.readInt("Año Académico", 2024, 2030);
        String obs = TerminalReader.readOptionalString("Observaciones de Matrícula");

        boolean ok = controller.enrollStudent(student.getId(), gradeId, year, LocalDate.now(), obs);
        if (ok) {
            System.out.println("  [✓] Matrícula formalizada exitosamente para el año " + year + ".");
        } else {
            System.out.println("  [✗] No fue posible registrar la matrícula.");
        }
        TerminalReader.pause();
    }
}
