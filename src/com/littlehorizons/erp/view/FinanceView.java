package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.TerminalReader;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FinanceView {

    private final FinanceController controller;

    public FinanceView(FinanceController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN FINANCIERA Y FACTURACIÓN             ║");
            System.out.println("║     (Marco Tributario: Estatuto Tributario y DIAN)           ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Catálogo de Conceptos de Pago y Tarifas");
            System.out.println("  2. Registrar Pago y Generar Factura Electrónica (PDF/TXT/XML)");
            System.out.println("  3. Historial de Pagos de un Estudiante");
            System.out.println("  4. Generar Factura Electrónica a partir de un Pago Existente");
            System.out.println("  5. Reporte de Estudiantes con Pagos Pendientes (Morosos)");
            System.out.println("  6. Listar Facturas Electrónicas Expedidas");
            System.out.println("  7. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 7);
            switch (option) {
                case 1:
                    listConcepts();
                    break;
                case 2:
                    recordPaymentAndInvoice();
                    break;
                case 3:
                    viewStudentPayments();
                    break;
                case 4:
                    generateInvoiceForExistingPayment();
                    break;
                case 5:
                    viewDebtors();
                    break;
                case 6:
                    listInvoices();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private void listConcepts() {
        System.out.println("\n--- CATÁLOGO DE CONCEPTOS DE PAGO (EXENCIÓN IVA ART. 476 E.T.) ---");
        List<PaymentConcept> concepts = controller.listAllConcepts();
        System.out.printf("%-4s | %-12s | %-30s | %-16s | %-12s%n",
                "ID", "CÓDIGO", "CONCEPTO", "VALOR BASE (COP)", "PERIODICIDAD");
        System.out.println("----------------------------------------------------------------------------------");
        for (PaymentConcept c : concepts) {
            System.out.printf("%-4d | %-12s | %-30s | $%,14.2f | %-12s%n",
                    c.getId(), c.getCode(), c.getName(), c.getBaseAmount(), c.isRecurring() ? "Mensual" : "Pago Único");
        }
        TerminalReader.pause();
    }

    private void recordPaymentAndInvoice() {
        System.out.println("\n--- REGISTRAR PAGO Y GENERAR FACTURA ---");
        int studentId = TerminalReader.readInt("ID del Estudiante beneficiario", 1, 1000);
        int guardianId = TerminalReader.readInt("ID del Acudiente responsable", 1, 1000);

        List<PaymentConcept> concepts = controller.listAllConcepts();
        System.out.println("Conceptos disponibles:");
        for (PaymentConcept c : concepts) {
            System.out.printf("  [%d] %s ($%,.2f COP)%n", c.getId(), c.getName(), c.getBaseAmount());
        }

        int conceptId = TerminalReader.readInt("Seleccione ID del Concepto", 1, 20);
        double amount = TerminalReader.readDouble("Monto a pagar en COP", 1000.0, 10000000.0);

        System.out.println("Método de pago:");
        System.out.println(" 1. Transferencia  |  2. Tarjeta Débito  |  3. Tarjeta Crédito  |  4. Efectivo  |  5. Cheque");
        int mOpt = TerminalReader.readInt("Seleccione", 1, 5);
        String[] methods = {"Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Efectivo", "Cheque"};
        String method = methods[mOpt - 1];

        int year = TerminalReader.readInt("Año Académico", 2024, 2030);
        String period = TerminalReader.readNonEmptyString("Período Académico (I, II, III, IV, Anual)");
        String notes = TerminalReader.readOptionalString("Notas adicionales");

        try {
            Payment payment = controller.recordPayment(studentId, guardianId, conceptId, amount, method, year, period, notes);
            System.out.println("\n  [✓] Pago registrado exitosamente con Recibo: " + payment.getReceiptNumber());

            // Multi-format invoice generation selection
            promptInvoiceExport(payment);

        } catch (Exception e) {
            System.out.println("  [✗] Error al procesar pago: " + e.getMessage());
        }
        TerminalReader.pause();
    }

    private void promptInvoiceExport(Payment payment) {
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("   GENERACIÓN DE FACTURA ELECTRÓNICA DE VENTA (DIAN)          ");
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("Seleccione el formato deseado para la emisión de la factura:");
        System.out.println("  1. Formato PDF (Documento visual profesional con OpenPDF)");
        System.out.println("  2. Formato TXT (Estructurado para Agentes de Evaluación)");
        System.out.println("  3. Formato XML (Estándar UBL 2.1 / MUISCA DIAN)");
        System.out.println("  4. TODOS los Formatos Simultáneamente (PDF + TXT + XML)");
        System.out.println("  5. Omitir generación de factura por ahora");
        System.out.println("──────────────────────────────────────────────────────────────");

        int choice = TerminalReader.readInt("Seleccione opción", 1, 5);
        String formatKey = null;
        switch (choice) {
            case 1: formatKey = "PDF"; break;
            case 2: formatKey = "TXT"; break;
            case 3: formatKey = "XML"; break;
            case 4: formatKey = "ALL"; break;
            case 5: return;
        }

        try {
            Map<String, String> outputs = controller.generateInvoice(payment, formatKey);
            System.out.println("\n  [✓] ¡Facturación electrónica completada con éxito!");
            outputs.forEach((fmt, path) -> {
                System.out.printf("      • Formato %s: %s%n", fmt, path);
            });
        } catch (Exception e) {
            System.out.println("  [✗] Ocurrió un error al generar la factura: " + e.getMessage());
        }
    }

    private void generateInvoiceForExistingPayment() {
        System.out.println("\n--- FACTURAR PAGO EXISTENTE ---");
        List<Payment> payments = controller.listAllPayments();
        if (payments.isEmpty()) {
            System.out.println("No hay pagos registrados.");
            TerminalReader.pause();
            return;
        }

        System.out.println("Últimos pagos registrados:");
        for (int i = 0; i < Math.min(payments.size(), 10); i++) {
            Payment p = payments.get(i);
            System.out.printf("  [%d] Recibo: %s | Estudiante: %s | Monto: $%,.2f COP%n",
                    p.getId(), p.getReceiptNumber(), p.getStudentName(), p.getPaidAmount());
        }

        int paymentId = TerminalReader.readInt("ID del Pago a facturar", 1, 10000);
        Optional<Payment> target = payments.stream().filter(p -> p.getId() == paymentId).findFirst();
        if (target.isPresent()) {
            promptInvoiceExport(target.get());
        } else {
            System.out.println("  [!] Pago no encontrado.");
        }
        TerminalReader.pause();
    }

    private void viewStudentPayments() {
        System.out.println("\n--- HISTORIAL FINANCIERO DEL ESTUDIANTE ---");
        int studentId = TerminalReader.readInt("ID del Estudiante", 1, 1000);
        List<Payment> payments = controller.getPaymentsByStudent(studentId);
        if (payments.isEmpty()) {
            System.out.println("  (El estudiante no registra pagos)");
        } else {
            System.out.printf("%-12s | %-16s | %-24s | %-16s | %-14s%n",
                    "FECHA", "RECIBO", "CONCEPTO", "VALOR PAGADO", "MÉTODO");
            System.out.println("------------------------------------------------------------------------------------------");
            for (Payment p : payments) {
                System.out.printf("%-12s | %-16s | %-24s | $%,14.2f | %-14s%n",
                        p.getPaymentDate(), p.getReceiptNumber(), p.getConceptName(), p.getPaidAmount(), p.getPaymentMethod());
            }
        }
        TerminalReader.pause();
    }

    private void viewDebtors() {
        System.out.println("\n--- REPORTE DE ESTUDIANTES CON PAGOS PENDIENTES (CARTERA / MOROSOS) ---");
        int year = TerminalReader.readInt("Año Académico", 2024, 2030);
        String period = TerminalReader.readValidatedString("Período a verificar (I, II, III, IV)",
                p -> p.matches("I|II|III|IV"), "Período inválido (I, II, III, IV)");

        List<Student> debtors = controller.getDebtorStudents(year, period);
        if (debtors.isEmpty()) {
            System.out.println("  [✓] ¡Excelente! No se registran estudiantes morosos en el período " + period + ".");
        } else {
            System.out.printf("Se identificaron %d estudiante(s) con pensión pendiente en el período %s:%n%n", debtors.size(), period);
            System.out.printf("%-5s | %-12s | %-32s | %-12s%n", "ID", "DOCUMENTO", "NOMBRE COMPLETO", "TELÉFONO");
            System.out.println("--------------------------------------------------------------------");
            for (Student s : debtors) {
                System.out.printf("%-5d | %-12s | %-32s | %-12s%n",
                        s.getId(), s.getDocumentNumber(), s.getFullName(), s.getPhone() != null ? s.getPhone() : "N/A");
            }
        }
        TerminalReader.pause();
    }

    private void listInvoices() {
        System.out.println("\n--- FACTURAS ELECTRÓNICAS EXPEDIDAS ---");
        List<Invoice> invoices = controller.listAllInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No se han emitido facturas aún.");
        } else {
            System.out.printf("%-12s | %-12s | %-30s | %-25s%n",
                    "FACTURA N°", "EMISIÓN", "CUFE (SHA-384)", "ARCHIVO PDF");
            System.out.println("----------------------------------------------------------------------------------------");
            for (Invoice inv : invoices) {
                String shortCufe = inv.getCufe() != null && inv.getCufe().length() > 24
                        ? inv.getCufe().substring(0, 24) + "..." : inv.getCufe();
                System.out.printf("%-12s | %-12s | %-30s | %-25s%n",
                        inv.getInvoiceNumber(), inv.getIssueDate(), shortCufe, inv.getPdfFileName());
            }
        }
        TerminalReader.pause();
    }
}
