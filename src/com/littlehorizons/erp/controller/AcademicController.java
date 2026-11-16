package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.SubjectDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AcademicController {

    private final AcademicService academicService;
    private final AttendanceService attendanceService;
    private final SubjectDAO subjectDAO;
    private final StudentDAO studentDAO;

    public AcademicController() {
        this.academicService = new AcademicService();
        this.attendanceService = new AttendanceService();
        this.subjectDAO = new SubjectDAO();
        this.studentDAO = new StudentDAO();
    }

    public boolean registerGrade(int studentId, int subjectId, int teacherId, int year,
                                 String period, double gradeValue, String observations) {
        return academicService.registerGrade(studentId, subjectId, teacherId, year, period, gradeValue, observations);
    }

    public double getStudentPeriodAverage(int studentId, int year, String period) {
        return academicService.calculateStudentPeriodAverage(studentId, year, period);
    }

    public double getSubjectAverage(int studentId, int subjectId, int year) {
        return academicService.calculateSubjectAverage(studentId, subjectId, year);
    }

    public double getCourseAverage(int subjectId, int year, String period) {
        return academicService.calculateCourseAverage(subjectId, year, period);
    }

    public Map<Integer, List<AcademicRecord>> getStudentAcademicHistory(int studentId) {
        return academicService.getStudentAcademicHistory(studentId);
    }

    public String getMenQualitativeConcept(double grade) {
        return academicService.getMenQualitativeConcept(grade);
    }

    public boolean recordAttendance(int studentId, int subjectId, LocalDate date, String status, String obs, Integer teacherId) {
        return attendanceService.recordAttendance(studentId, subjectId, date, status, obs, teacherId);
    }

    public List<Attendance> getStudentAttendanceHistory(int studentId) {
        return attendanceService.getStudentAttendanceHistory(studentId);
    }

    public boolean checkChronicAbsences(int studentId, LocalDate referenceDate) {
        return attendanceService.hasChronicWeeklyAbsences(studentId, referenceDate);
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    public Optional<Student> findStudentByDocument(String doc) {
        return studentDAO.findByDocument(doc);
    }
}
