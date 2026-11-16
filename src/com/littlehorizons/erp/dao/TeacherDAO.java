package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, teacher.getDocumentType());
            ps.setString(2, teacher.getDocumentNumber());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getSecondName());
            ps.setString(5, teacher.getFirstLastName());
            ps.setString(6, teacher.getSecondLastName());
            ps.setString(7, teacher.getEmail());
            ps.setString(8, teacher.getPhone());
            ps.setString(9, teacher.getSpecialization());
            ps.setDate(10, Date.valueOf(teacher.getHireDate() != null ? teacher.getHireDate() : java.time.LocalDate.now()));
            ps.setString(11, teacher.getStatus() != null ? teacher.getStatus() : "Activo");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        teacher.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save teacher: " + teacher.getDocumentNumber(), e);
        }
        return false;
    }

    public Optional<Teacher> findById(int id) {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find teacher by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Teacher> findByDocument(String documentNumber) {
        String sql = "SELECT * FROM profesores WHERE numero_documento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find teacher by document: " + documentNumber, e);
        }
        return Optional.empty();
    }

    public List<Teacher> findAll() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM profesores ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTeacher(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all teachers", e);
        }
        return list;
    }

    public boolean assignSubject(int teacherId, int subjectId, int academicYear, String section) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE seccion = VALUES(seccion)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYear);
            ps.setString(4, section != null ? section : "A");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.logError("Failed to assign subject " + subjectId + " to teacher " + teacherId, e);
        }
        return false;
    }

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setSecondName(rs.getString("segundo_nombre"));
        t.setFirstLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        Date hire = rs.getDate("fecha_contratacion");
        if (hire != null) t.setHireDate(hire.toLocalDate());
        t.setStatus(rs.getString("estado"));
        return t;
    }
}
