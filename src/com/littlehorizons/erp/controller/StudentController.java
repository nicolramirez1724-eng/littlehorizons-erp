package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentController {

    private final StudentDAO studentDAO;
    private final GuardianDAO guardianDAO;

    public StudentController() {
        this.studentDAO = new StudentDAO();
        this.guardianDAO = new GuardianDAO();
    }

    public List<Student> listAllStudents() {
        return studentDAO.findAll();
    }

    public Optional<Student> getStudentByDocument(String documentNumber) {
        return studentDAO.findByDocument(documentNumber);
    }

    public Optional<Student> getStudentById(int id) {
        return studentDAO.findById(id);
    }

    public boolean registerStudent(Student student) {
        return studentDAO.save(student);
    }

    public boolean registerGuardian(Guardian guardian) {
        return guardianDAO.save(guardian);
    }

    public boolean linkStudentGuardian(int studentId, int guardianId, boolean isMain, boolean isEmergency) {
        return studentDAO.linkGuardian(studentId, guardianId, isMain, isEmergency);
    }

    public boolean enrollStudent(int studentId, int gradeId, int academicYear, LocalDate date, String obs) {
        return studentDAO.enrollInGrade(studentId, gradeId, academicYear, date, obs);
    }

    public List<Guardian> getGuardiansByStudent(int studentId) {
        return guardianDAO.findByStudentId(studentId);
    }
}
