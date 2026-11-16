package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.Teacher;

import java.util.List;
import java.util.Optional;

public class TeacherController {

    private final TeacherDAO teacherDAO;

    public TeacherController() {
        this.teacherDAO = new TeacherDAO();
    }

    public List<Teacher> listAllTeachers() {
        return teacherDAO.findAll();
    }

    public Optional<Teacher> getTeacherByDocument(String docNumber) {
        return teacherDAO.findByDocument(docNumber);
    }

    public boolean registerTeacher(Teacher teacher) {
        return teacherDAO.save(teacher);
    }

    public boolean assignSubject(int teacherId, int subjectId, int academicYear, String section) {
        return teacherDAO.assignSubject(teacherId, subjectId, academicYear, section);
    }
}
