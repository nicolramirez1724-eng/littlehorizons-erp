package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * AcademicRecord entity storing numeric grades (0.0 to 5.0)
 * and mapping directly to Colombian MEN Decree 1290/2009 evaluation scale.
 */
public class AcademicRecord {
    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private String academicPeriod; // I, II, III, IV, Final
    private double gradeValue;
    private String observations;
    private LocalDate registerDate;

    // Optional descriptive fields populated in joins
    private String studentName;
    private String subjectName;
    private String teacherName;

    public AcademicRecord() {}

    public AcademicRecord(int id, int studentId, int subjectId, int teacherId, int academicYear,
                          String academicPeriod, double gradeValue, String observations, LocalDate registerDate) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.gradeValue = gradeValue;
        this.observations = observations;
        this.registerDate = registerDate;
    }

    /**
     * Obtains the official qualitative performance level according to Colombian MEN Decree 1290/2009.
     *
     * @return Desempeño Superior, Alto, Básico, or Bajo
     */
    public String getMenPerformanceLevel() {
        return getMenPerformanceLevel(this.gradeValue);
    }

    public static String getMenPerformanceLevel(double grade) {
        if (grade >= 4.6 && grade <= 5.0) {
            return "Desempeño Superior";
        } else if (grade >= 4.0) {
            return "Desempeño Alto";
        } else if (grade >= 3.0) {
            return "Desempeño Básico";
        } else {
            return "Desempeño Bajo (Reprobado)";
        }
    }

    public boolean isApproved() {
        return this.gradeValue >= 3.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public double getGradeValue() { return gradeValue; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public LocalDate getRegisterDate() { return registerDate; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return String.format("Periodo %s: %.1f [%s]", academicPeriod, gradeValue, getMenPerformanceLevel());
    }
}
