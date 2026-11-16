package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuardianDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Guardian guardian) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, " +
                "correo_electronico, direccion, ocupacion, es_responsable_financiero) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guardian.getDocumentType());
            ps.setString(2, guardian.getDocumentNumber());
            ps.setString(3, guardian.getFirstName());
            ps.setString(4, guardian.getSecondName());
            ps.setString(5, guardian.getFirstLastName());
            ps.setString(6, guardian.getSecondLastName());
            ps.setString(7, guardian.getRelationship());
            ps.setString(8, guardian.getMainPhone());
            ps.setString(9, guardian.getSecondaryPhone());
            ps.setString(10, guardian.getEmail());
            ps.setString(11, guardian.getAddress());
            ps.setString(12, guardian.getOccupation());
            ps.setBoolean(13, guardian.isFinancialResponsible());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        guardian.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save guardian: " + guardian.getDocumentNumber(), e);
        }
        return false;
    }

    public Optional<Guardian> findById(int id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find guardian by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Guardian> findByDocument(String documentNumber) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find guardian by document: " + documentNumber, e);
        }
        return Optional.empty();
    }

    public List<Guardian> findByStudentId(int studentId) {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT a.* FROM acudientes a " +
                "INNER JOIN estudiante_acudiente ea ON a.id_acudiente = ea.id_acudiente " +
                "WHERE ea.id_estudiante = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find guardians for student: " + studentId, e);
        }
        return list;
    }

    public List<Guardian> findAll() {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGuardian(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all guardians", e);
        }
        return list;
    }

    private Guardian mapResultSetToGuardian(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setSecondName(rs.getString("segundo_nombre"));
        g.setFirstLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setMainPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }
}
