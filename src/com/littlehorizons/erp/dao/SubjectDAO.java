package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getCode());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getGradeId());
            ps.setInt(5, subject.getWeeklyHours());
            ps.setBoolean(6, subject.isActive());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        subject.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save subject: " + subject.getCode(), e);
        }
        return false;
    }

    public Optional<Subject> findById(int id) {
        String sql = "SELECT * FROM materias WHERE id_materia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find subject by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Subject> findByCode(String code) {
        String sql = "SELECT * FROM materias WHERE codigo_materia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find subject by code: " + code, e);
        }
        return Optional.empty();
    }

    public List<Subject> findByGrade(int gradeId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM materias WHERE id_grado = ? AND esta_activa = TRUE ORDER BY nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find subjects for grade: " + gradeId, e);
        }
        return list;
    }

    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM materias ORDER BY id_grado, nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to fetch all subjects", e);
        }
        return list;
    }

    public boolean enrollStudent(int studentId, int subjectId, int year, String period, String section) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'Activa') ON DUPLICATE KEY UPDATE estado = 'Activa'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, period);
            ps.setString(5, section != null ? section : "A");
            ps.setDate(6, Date.valueOf(LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.logError("Failed to enroll student " + studentId + " in subject " + subjectId, e);
        }
        return false;
    }

    public List<Subject> findEnrolledSubjects(int studentId, int year, String period) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT m.* FROM materias m " +
                "INNER JOIN inscripciones_materia im ON m.id_materia = im.id_materia " +
                "WHERE im.id_estudiante = ? AND im.año_academico = ? AND im.periodo_academico = ? AND im.estado = 'Activa'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to fetch enrolled subjects for student " + studentId, e);
        }
        return list;
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id_materia"));
        s.setCode(rs.getString("codigo_materia"));
        s.setName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setGradeId(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        return s;
    }
}
