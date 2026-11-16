package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.AcademicRecordDAO;
import com.littlehorizons.erp.dao.SubjectDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Business service for academic management.
 * Implements Colombian MEN Decree 1290/2009 evaluation rules and Stream API calculations.
 */
public class AcademicService {

    private final AcademicRecordDAO academicRecordDAO;
    private final SubjectDAO subjectDAO;

    public AcademicService() {
        this.academicRecordDAO = new AcademicRecordDAO();
        this.subjectDAO = new SubjectDAO();
    }

    public AcademicService(AcademicRecordDAO academicRecordDAO, SubjectDAO subjectDAO) {
        this.academicRecordDAO = academicRecordDAO;
        this.subjectDAO = subjectDAO;
    }

    /**
     * Registers or updates a student grade with rigorous validation.
     */
    public boolean registerGrade(int studentId, int subjectId, int teacherId, int academicYear,
                                 String period, double gradeValue, String observations) {
        if (!InputValidator.isValidGrade(gradeValue)) {
            throw new IllegalArgumentException("La nota debe estar en el rango de 0.0 a 5.0 según el estándar MEN.");
        }
        if (!InputValidator.isValidPeriod(period)) {
            throw new IllegalArgumentException("Período académico inválido (I, II, III, IV, Final).");
        }

        AcademicRecord record = new AcademicRecord(0, studentId, subjectId, teacherId, academicYear,
                period, Math.round(gradeValue * 10.0) / 10.0, observations, LocalDate.now());
        return academicRecordDAO.save(record);
    }

    /**
     * Calculates the student's average in a specific subject over an academic year using Streams.
     */
    public double calculateSubjectAverage(int studentId, int subjectId, int academicYear) {
        List<AcademicRecord> records = academicRecordDAO.findByStudentAndYear(studentId, academicYear);
        OptionalDouble avg = records.stream()
                .filter(r -> r.getSubjectId() == subjectId)
                .mapToDouble(AcademicRecord::getGradeValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    /**
     * Calculates the student's general average in a given period and academic year using Streams.
     */
    public double calculateStudentPeriodAverage(int studentId, int academicYear, String period) {
        List<AcademicRecord> records = academicRecordDAO.findByStudentAndYear(studentId, academicYear);
        OptionalDouble avg = records.stream()
                .filter(r -> period.equalsIgnoreCase(r.getAcademicPeriod()))
                .mapToDouble(AcademicRecord::getGradeValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    /**
     * Calculates the course average for a specific subject in a period using Streams.
     */
    public double calculateCourseAverage(int subjectId, int academicYear, String period) {
        List<AcademicRecord> records = academicRecordDAO.findBySubjectAndYearPeriod(subjectId, academicYear, period);
        OptionalDouble avg = records.stream()
                .mapToDouble(AcademicRecord::getGradeValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    /**
     * Retrieves the complete academic history of a student grouped by academic year.
     */
    public Map<Integer, List<AcademicRecord>> getStudentAcademicHistory(int studentId) {
        List<AcademicRecord> records = academicRecordDAO.findByStudent(studentId);
        return records.stream()
                .collect(Collectors.groupingBy(AcademicRecord::getAcademicYear));
    }

    /**
     * Obtains the official qualitative label (MEN Decreto 1290) for any numeric average.
     */
    public String getMenQualitativeConcept(double grade) {
        return AcademicRecord.getMenPerformanceLevel(grade);
    }
}
