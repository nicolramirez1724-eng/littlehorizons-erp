package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicRecordDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, " +
                "año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE valor_nota = VALUES(valor_nota), observaciones = VALUES(observaciones), fecha_actualizacion = CURRENT_TIMESTAMP";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, record.getStudentId());
            ps.setInt(2, record.getSubjectId());
            ps.setInt(3, record.getTeacherId());
            ps.setInt(4, record.getAcademicYear());
            ps.setString(5, record.getAcademicPeriod());
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(record.getGradeValue()));
            ps.setString(7, record.getObservations());
            ps.setDate(8, Date.valueOf(record.getRegisterDate() != null ? record.getRegisterDate() : LocalDate.now()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        record.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save academic record for student " + record.getStudentId(), e);
        }
        return false;
    }

    public List<AcademicRecord> findByStudent(int studentId) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos ra " +
                "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante " +
                "INNER JOIN materias m ON ra.id_materia = m.id_materia " +
                "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor " +
                "WHERE ra.id_estudiante = ? " +
                "ORDER BY ra.año_academico DESC, ra.periodo_academico, m.nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find academic records for student " + studentId, e);
        }
        return list;
    }

    public List<AcademicRecord> findByStudentAndYear(int studentId, int year) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos ra " +
                "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante " +
                "INNER JOIN materias m ON ra.id_materia = m.id_materia " +
                "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor " +
                "WHERE ra.id_estudiante = ? AND ra.año_academico = ? " +
                "ORDER BY ra.periodo_academico, m.nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find records for student " + studentId + " in year " + year, e);
        }
        return list;
    }

    public List<AcademicRecord> findBySubjectAndYearPeriod(int subjectId, int year, String period) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos ra " +
                "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante " +
                "INNER JOIN materias m ON ra.id_materia = m.id_materia " +
                "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor " +
                "WHERE ra.id_materia = ? AND ra.año_academico = ? AND ra.periodo_academico = ? " +
                "ORDER BY e.primer_apellido, e.primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find records for subject " + subjectId, e);
        }
        return list;
    }

    private AcademicRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setId(rs.getInt("id_registro"));
        r.setStudentId(rs.getInt("id_estudiante"));
        r.setSubjectId(rs.getInt("id_materia"));
        r.setTeacherId(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("año_academico"));
        r.setAcademicPeriod(rs.getString("periodo_academico"));
        r.setGradeValue(rs.getBigDecimal("valor_nota").doubleValue());
        r.setObservations(rs.getString("observaciones"));
        Date reg = rs.getDate("fecha_registro");
        if (reg != null) r.setRegisterDate(reg.toLocalDate());
        r.setStudentName(rs.getString("nom_estudiante"));
        r.setSubjectName(rs.getString("nombre_materia"));
        r.setTeacherName(rs.getString("nom_profesor"));
        return r;
    }
}
