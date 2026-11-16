package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.util.TerminalReader;

import java.time.LocalDate;
import java.util.List;

public class ResourceView {

    private final ResourceController controller;

    public ResourceView(ResourceController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN DE RECURSOS (BIBLIOTECA/LAB)         ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Catálogo Completo de Recursos e Inventario");
            System.out.println("  2. Registrar Préstamo de Recurso a Estudiante");
            System.out.println("  3. Registrar Devolución de Recurso Prestado");
            System.out.println("  4. Reporte de Préstamos Vencidos (Seguimiento)");
            System.out.println("  5. Registrar Nuevo Recurso en Inventario");
            System.out.println("  6. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 6);
            switch (option) {
                case 1:
                    listResources();
                    break;
                case 2:
                    loanResource();
                    break;
                case 3:
                    returnResource();
                    break;
                case 4:
                    viewOverdueLoans();
                    break;
                case 5:
                    registerResource();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private void listResources() {
        System.out.println("\n--- INVENTARIO GENERAL DE RECURSOS PRESTABLES ---");
        List<Resource> resources = controller.listAllResources();
        if (resources.isEmpty()) {
            System.out.println("No hay recursos registrados en el inventario.");
        } else {
            System.out.printf("%-4s | %-10s | %-38s | %-18s | %-16s | %-12s%n",
                    "ID", "CÓDIGO", "NOMBRE DEL RECURSO", "CATEGORÍA", "UBICACIÓN", "ESTADO");
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            for (Resource r : resources) {
                System.out.printf("%-4d | %-10s | %-38s | %-18s | %-16s | %-12s%n",
                        r.getId(), r.getCode(), r.getName(), r.getCategoryName(), r.getLocation(), r.getStatus());
            }
        }
        TerminalReader.pause();
    }

    private void loanResource() {
        System.out.println("\n--- REGISTRAR PRÉSTAMO DE RECURSO ---");
        int resourceId = TerminalReader.readInt("ID del Recurso a prestar", 1, 1000);
        int studentId = TerminalReader.readInt("ID del Estudiante solicitante", 1, 1000);
        LocalDate returnDate = TerminalReader.readDate("Fecha estimada de devolución");
        String obs = TerminalReader.readOptionalString("Observaciones sobre el estado del préstamo");

        try {
            boolean ok = controller.loanToStudent(resourceId, studentId, returnDate, obs);
            if (ok) {
                System.out.println("  [✓] Préstamo formalizado exitosamente. El recurso ahora figura como 'Prestado'.");
            } else {
                System.out.println("  [✗] No fue posible registrar el préstamo.");
            }
        } catch (Exception e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        TerminalReader.pause();
    }

    private void returnResource() {
        System.out.println("\n--- REGISTRAR DEVOLUCIÓN DE RECURSO ---");
        int loanId = TerminalReader.readInt("ID del Préstamo a cerrar", 1, 10000);
        boolean ok = controller.returnLoan(loanId);
        if (ok) {
            System.out.println("  [✓] Devolución procesada correctamente. El recurso ha retornado al estado 'Disponible'.");
        } else {
            System.out.println("  [✗] No se encontró el préstamo o ya se encontraba devuelto.");
        }
        TerminalReader.pause();
    }

    private void viewOverdueLoans() {
        System.out.println("\n--- REPORTE DE PRÉSTAMOS VENCIDOS (BIBLIOTECA Y LABORATORIOS) ---");
        List<StudentLoan> overdue = controller.getOverdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("  [✓] ¡Excelente! No hay préstamos vencidos en este momento.");
        } else {
            System.out.printf("%-5s | %-32s | %-28s | %-14s | %-10s%n",
                    "ID", "RECURSO", "ESTUDIANTE", "VENCIMIENTO", "ESTADO");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (StudentLoan l : overdue) {
                System.out.printf("%-5d | %-32s | %-28s | %-14s | %-10s%n",
                        l.getId(), l.getResourceName(), l.getStudentName(), l.getExpectedReturnDate(), l.getStatus());
            }
        }
        TerminalReader.pause();
    }

    private void registerResource() {
        System.out.println("\n--- REGISTRAR NUEVO RECURSO EN INVENTARIO ---");
        String code = TerminalReader.readNonEmptyString("Código de Inventario Único (Ej: LIB-025, LAB-015)");
        String name = TerminalReader.readNonEmptyString("Nombre o Título del Recurso");

        List<ResourceCategory> cats = controller.listAllCategories();
        System.out.println("Categorías disponibles:");
        for (ResourceCategory c : cats) {
            System.out.printf("  [%d] %s%n", c.getId(), c.getName());
        }

        int catId = TerminalReader.readInt("Seleccione ID de Categoría", 1, 10);
        String desc = TerminalReader.readOptionalString("Descripción detallada");
        LocalDate acqDate = TerminalReader.readDate("Fecha de Adquisición");
        String location = TerminalReader.readNonEmptyString("Ubicación Física (Ej: Biblioteca Estante L3, Laboratorio 2)");

        boolean ok = controller.registerResource(code, name, catId, desc, acqDate, location);
        if (ok) {
            System.out.println("  [✓] Recurso ingresado exitosamente al inventario.");
        } else {
            System.out.println("  [✗] Error al registrar recurso en base de datos.");
        }
        TerminalReader.pause();
    }
}
