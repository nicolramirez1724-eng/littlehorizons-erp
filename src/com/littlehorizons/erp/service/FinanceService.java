package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.InvoiceDAO;
import com.littlehorizons.erp.dao.PaymentDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.service.export.InvoiceExportManager;
import com.littlehorizons.erp.util.CufeGenerator;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing financial payments, receipt generation,
 * DIAN electronic invoice orchestration, and debtor reporting.
 */
public class FinanceService {

    private final PaymentDAO paymentDAO;
    private final InvoiceDAO invoiceDAO;
    private final StudentDAO studentDAO;
    private final InvoiceExportManager exportManager;

    public FinanceService() {
        this.paymentDAO = new PaymentDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.studentDAO = new StudentDAO();
        this.exportManager = new InvoiceExportManager();
    }

    public FinanceService(PaymentDAO paymentDAO, InvoiceDAO invoiceDAO, StudentDAO studentDAO, InvoiceExportManager exportManager) {
        this.paymentDAO = paymentDAO;
        this.invoiceDAO = invoiceDAO;
        this.studentDAO = studentDAO;
        this.exportManager = exportManager;
    }

    public List<PaymentConcept> getAllConcepts() {
        return paymentDAO.findAllConcepts();
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }

    public List<Payment> getPaymentsByStudent(int studentId) {
        return paymentDAO.findByStudent(studentId);
    }

    /**
     * Processes and records a student fee payment.
     */
    public Payment processPayment(int studentId, Integer guardianId, int conceptId, double paidAmount,
                                  String paymentMethod, Integer academicYear, String academicPeriod, String notes) {
        if (!InputValidator.isValidAmount(paidAmount)) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor o igual a cero.");
        }
        if (!InputValidator.isValidPaymentMethod(paymentMethod)) {
            throw new IllegalArgumentException("Método de pago no reconocido.");
        }

        String receiptNumber = "REC-" + LocalDate.now().getYear() + "-" + String.format("%04d", (int)(Math.random() * 9000 + 1000));

        Payment payment = new Payment(0, studentId, guardianId, conceptId, paidAmount,
                LocalDate.now(), paymentMethod, receiptNumber, academicYear, academicPeriod, notes);

        boolean saved = paymentDAO.save(payment);
        if (!saved) {
            throw new RuntimeException("No fue posible registrar el pago en la base de datos.");
        }

        // Return re-queried payment with joined view fields
        return paymentDAO.findById(payment.getId()).orElse(payment);
    }

    /**
     * Issues a DIAN electronic invoice for a payment and exports it in the chosen format.
     * Format option: "PDF", "TXT", "XML", or "ALL".
     */
    public Map<String, String> generateInvoice(Payment payment, String exportChoice) throws Exception {
        String invoiceNumber = invoiceDAO.getNextInvoiceNumber();
        LocalDate issueDate = LocalDate.now();
        String issueTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        double subtotal = payment.getPaidAmount();
        double iva = 0.00; // Art. 476 Num 6 ET: Excluido
        double total = subtotal + iva;

        String guardianDoc = payment.getGuardianDocNumber() != null ? payment.getGuardianDocNumber() : "222222222222";
        String cufe = CufeGenerator.generateCufe(invoiceNumber, issueDate.toString(), issueTime, subtotal, iva, total, guardianDoc);

        String pdfFileName = "factura_" + invoiceNumber + ".pdf";
        String filePath = "invoices/" + pdfFileName;

        Invoice invoice = new Invoice(0, invoiceNumber, payment.getId(), issueDate, cufe, pdfFileName, filePath);
        invoice.setPayment(payment);

        invoiceDAO.save(invoice);

        Map<String, String> generatedFiles = new LinkedHashMap<>();
        if ("ALL".equalsIgnoreCase(exportChoice)) {
            generatedFiles = exportManager.exportAll(invoice, payment);
        } else {
            String path = exportManager.exportSingle(exportChoice, invoice, payment);
            generatedFiles.put(exportChoice.toUpperCase(), path);
        }

        return generatedFiles;
    }

    /**
     * Generates a report of students with pending monthly tuition payments in an academic year.
     */
    public List<Student> getDebtorStudents(int academicYear, String academicPeriod) {
        List<Student> allStudents = studentDAO.findAll();
        List<Payment> payments = paymentDAO.findAll();

        // Filter students that have NOT made a tuition payment for this period/year
        Set<Integer> paidStudentIds = payments.stream()
                .filter(p -> p.getAcademicYear() != null && p.getAcademicYear() == academicYear)
                .filter(p -> academicPeriod == null || academicPeriod.equalsIgnoreCase(p.getAcademicPeriod()))
                .filter(p -> "PENSION".equalsIgnoreCase(p.getConceptCode()) || p.getConceptId() == 2)
                .map(Payment::getStudentId)
                .collect(Collectors.toSet());

        return allStudents.stream()
                .filter(s -> "Activo".equalsIgnoreCase(s.getStatus()))
                .filter(s -> !paidStudentIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }
}
