package com.littlehorizons.erp.model;

import java.time.LocalDate;

public class Payment {
    private int id;
    private int studentId;
    private Integer guardianId;
    private int conceptId;
    private double paidAmount;
    private LocalDate paymentDate;
    private String paymentMethod; // Efectivo, Transferencia, Tarjeta Débito, Tarjeta Crédito, Cheque
    private String receiptNumber;
    private Integer academicYear;
    private String academicPeriod; // I, II, III, IV, Anual
    private String notes;

    // Join view fields
    private String studentName;
    private String studentDocNumber;
    private String guardianName;
    private String guardianDocNumber;
    private String guardianAddress;
    private String guardianPhone;
    private String conceptName;
    private String conceptCode;

    public Payment() {}

    public Payment(int id, int studentId, Integer guardianId, int conceptId, double paidAmount,
                   LocalDate paymentDate, String paymentMethod, String receiptNumber,
                   Integer academicYear, String academicPeriod, String notes) {
        this.id = id;
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.conceptId = conceptId;
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getGuardianId() { return guardianId; }
    public void setGuardianId(Integer guardianId) { this.guardianId = guardianId; }

    public int getConceptId() { return conceptId; }
    public void setConceptId(int conceptId) { this.conceptId = conceptId; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentDocNumber() { return studentDocNumber; }
    public void setStudentDocNumber(String studentDocNumber) { this.studentDocNumber = studentDocNumber; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianDocNumber() { return guardianDocNumber; }
    public void setGuardianDocNumber(String guardianDocNumber) { this.guardianDocNumber = guardianDocNumber; }

    public String getGuardianAddress() { return guardianAddress; }
    public void setGuardianAddress(String guardianAddress) { this.guardianAddress = guardianAddress; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }

    public String getConceptName() { return conceptName; }
    public void setConceptName(String conceptName) { this.conceptName = conceptName; }

    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }

    @Override
    public String toString() {
        return String.format("[%s] Recibo: %s - %s - $%,.2f COP (%s)", paymentDate, receiptNumber, conceptName != null ? conceptName : "Concepto " + conceptId, paidAmount, paymentMethod);
    }
}
