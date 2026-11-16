package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.service.FinanceService;

import java.util.List;
import java.util.Map;

public class FinanceController {

    private final FinanceService service;

    public FinanceController() {
        this.service = new FinanceService();
    }

    public List<PaymentConcept> listAllConcepts() {
        return service.getAllConcepts();
    }

    public List<Payment> listAllPayments() {
        return service.getAllPayments();
    }

    public List<Payment> getPaymentsByStudent(int studentId) {
        return service.getPaymentsByStudent(studentId);
    }

    public Payment recordPayment(int studentId, Integer guardianId, int conceptId, double amount,
                                 String method, Integer year, String period, String notes) {
        return service.processPayment(studentId, guardianId, conceptId, amount, method, year, period, notes);
    }

    public Map<String, String> generateInvoice(Payment payment, String exportChoice) throws Exception {
        return service.generateInvoice(payment, exportChoice);
    }

    public List<Student> getDebtorStudents(int year, String period) {
        return service.getDebtorStudents(year, period);
    }

    public List<Invoice> listAllInvoices() {
        return service.getAllInvoices();
    }
}
