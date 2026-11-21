package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.CertificateController;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.LoggerManager;
import com.littlehorizons.erp.util.TerminalReader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CertificateView {

    private static final String LINE = "=".repeat(66);

    private final CertificateController controller;

    public CertificateView(CertificateController controller) {
        this.controller = controller;
    }

    public void generateCertificate() {
        int year = controller.getCertificateYear();
        int studentId = readStudentId();

        Optional<Student> studentOpt = controller.findStudent(studentId);
        if (studentOpt.isEmpty()) {
            System.out.println("Error: No se encontró ningún estudiante con el ID " + studentId
                    + ". Verifique el ID e intente nuevamente.");
            returnToMenu();
            return;
        }
        Student student = studentOpt.get();

        if (!controller.hasAcademicRecords(studentId)) {
            System.out.println("Error: El estudiante " + student.getFullName()
                    + " no tiene registros académicos para el año " + year + ".");
            returnToMenu();
            return;
        }

        System.out.println("Generando certificado para el año " + year + "...");
        System.out.println("Recopilando información académica...");

        try {
            File file = controller.generateCertificate(student);
            System.out.println();
            System.out.println(LINE);
            System.out.println("CERTIFICADO GENERADO EXITOSAMENTE");
            System.out.println(LINE);
            System.out.println("Estudiante: " + student.getFullName());
            System.out.println("Archivo generado: " + file.getName());
            System.out.println("Ubicación: " + file.getAbsolutePath());
            System.out.println("El certificado del año " + year + " está listo para imprimir.");
        } catch (IOException e) {
            LoggerManager.logError("Failed to write certificate file for student " + studentId, e);
            System.out.println();
            System.out.println(LINE);
            System.out.println("ERROR: No se pudo crear el archivo del certificado.");
            System.out.println("Verifique los permisos de escritura en la carpeta del proyecto.");
        }
        returnToMenu();
    }

    private int readStudentId() {
        while (true) {
            String input = TerminalReader.readLine("Ingrese el ID del estudiante para generar su certificado:");
            if (input.matches("\\d{1,9}")) {
                return Integer.parseInt(input);
            }
            System.out.println("Error: Debe ingresar un número válido.");
        }
    }

    private void returnToMenu() {
        System.out.println(LINE);
        TerminalReader.pause("Presione Enter para volver al menú principal...");
        System.out.println(LINE);
    }
}
