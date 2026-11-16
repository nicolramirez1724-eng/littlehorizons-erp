package com.littlehorizons.erp.model;

import java.time.LocalDate;

public class StudentLoan {
    private int id;
    private int resourceId;
    private int studentId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status; // Activo, Devuelto, Vencido
    private String observations;

    private String resourceName;
    private String resourceCode;
    private String studentName;

    public StudentLoan() {}

    public StudentLoan(int id, int resourceId, int studentId, LocalDate loanDate,
                       LocalDate expectedReturnDate, LocalDate actualReturnDate,
                       String status, String observations) {
        this.id = id;
        this.resourceId = resourceId;
        this.studentId = studentId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.observations = observations;
    }

    public boolean isOverdue() {
        return "Activo".equalsIgnoreCase(status) &&
                LocalDate.now().isAfter(expectedReturnDate);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    @Override
    public String toString() {
        return String.format("[%d] %s prestado a %s | Vence: %s | Estado: %s", id, resourceName != null ? resourceName : "Recurso " + resourceId, studentName != null ? studentName : "Est. " + studentId, expectedReturnDate, status);
    }
}
