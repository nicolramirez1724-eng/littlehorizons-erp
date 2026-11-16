package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, " +
                "observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), observaciones = VALUES(observaciones)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, attendance.getStudentId());
            ps.setInt(2, attendance.getSubjectId());
            ps.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            ps.setString(4, attendance.getStatus());
            ps.setString(5, attendance.getObservations());
            if (attendance.getRegisteredByTeacherId() != null) {
                ps.setInt(6, attendance.getRegisteredByTeacherId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        attendance.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save attendance", e);
        }
        return false;
    }

    public List<Attendance> findByStudent(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia FROM asistencias a " +
                "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                "INNER JOIN materias m ON a.id_materia = m.id_materia " +
                "WHERE a.id_estudiante = ? ORDER BY a.fecha_asistencia DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find attendance for student " + studentId, e);
        }
        return list;
    }

    public List<Attendance> findAbsencesByStudent(int studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia FROM asistencias a " +
                "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                "INNER JOIN materias m ON a.id_materia = m.id_materia " +
                "WHERE a.id_estudiante = ? AND a.estado_asistencia IN ('Ausente', 'Tardanza') " +
                "AND a.fecha_asistencia BETWEEN ? AND ? ORDER BY a.fecha_asistencia DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to query absences for student " + studentId, e);
        }
        return list;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id_asistencia"));
        a.setStudentId(rs.getInt("id_estudiante"));
        a.setSubjectId(rs.getInt("id_materia"));
        Date date = rs.getDate("fecha_asistencia");
        if (date != null) a.setAttendanceDate(date.toLocalDate());
        a.setStatus(rs.getString("estado_asistencia"));
        a.setObservations(rs.getString("observaciones"));
        int teacherId = rs.getInt("registrado_por");
        if (!rs.wasNull()) a.setRegisteredByTeacherId(teacherId);
        a.setStudentName(rs.getString("nom_estudiante"));
        a.setSubjectName(rs.getString("nombre_materia"));
        return a;
    }
}
