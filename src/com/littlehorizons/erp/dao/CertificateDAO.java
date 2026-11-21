package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.CertificateGrade;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CertificateDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public Optional<String> findEnrollmentGradeName(int studentId, int year) {
        String sql = "SELECT g.nombre_grado " +
                "FROM matriculas ma " +
                "INNER JOIN grados g ON ma.id_grado = g.id_grado " +
                "WHERE ma.id_estudiante = ? AND ma.año_academico = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("nombre_grado"));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find enrollment grade for student " + studentId + " in year " + year, e);
        }
        return Optional.empty();
    }

    public List<CertificateGrade> findGrades(int studentId, int year) {
        List<CertificateGrade> list = new ArrayList<>();
        String sql = "SELECT m.id_materia, m.codigo_materia, m.nombre_materia, " +
                "ra.periodo_academico, ra.valor_nota " +
                "FROM registros_academicos ra " +
                "INNER JOIN materias m ON ra.id_materia = m.id_materia " +
                "WHERE ra.id_estudiante = ? AND ra.año_academico = ? " +
                "AND ra.periodo_academico IN ('I', 'II', 'III', 'IV') " +
                "ORDER BY m.id_materia, ra.periodo_academico";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CertificateGrade(
                            rs.getInt("id_materia"),
                            rs.getString("codigo_materia"),
                            rs.getString("nombre_materia"),
                            rs.getString("periodo_academico"),
                            rs.getBigDecimal("valor_nota")));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find certificate grades for student " + studentId + " in year " + year, e);
        }
        return list;
    }
}
