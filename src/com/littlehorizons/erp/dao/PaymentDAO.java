package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Payment payment) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, " +
                "metodo_pago, numero_recibo, año_academico, periodo_academico, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, payment.getStudentId());
            if (payment.getGuardianId() != null) {
                ps.setInt(2, payment.getGuardianId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, payment.getConceptId());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(payment.getPaidAmount()));
            ps.setDate(5, Date.valueOf(payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now()));
            ps.setString(6, payment.getPaymentMethod());
            ps.setString(7, payment.getReceiptNumber());
            if (payment.getAcademicYear() != null) {
                ps.setInt(8, payment.getAcademicYear());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, payment.getAcademicPeriod());
            ps.setString(10, payment.getNotes());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save payment: " + payment.getReceiptNumber(), e);
        }
        return false;
    }

    public Optional<Payment> findById(int id) {
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "e.numero_documento AS doc_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, " +
                "a.numero_documento AS doc_acudiente, a.direccion AS dir_acudiente, a.telefono_principal AS tel_acudiente, " +
                "cp.nombre_concepto, cp.codigo_concepto " +
                "FROM pagos p " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto " +
                "WHERE p.id_pago = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find payment by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Payment> findByReceipt(String receiptNumber) {
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "e.numero_documento AS doc_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, " +
                "a.numero_documento AS doc_acudiente, a.direccion AS dir_acudiente, a.telefono_principal AS tel_acudiente, " +
                "cp.nombre_concepto, cp.codigo_concepto " +
                "FROM pagos p " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto " +
                "WHERE p.numero_recibo = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find payment by receipt: " + receiptNumber, e);
        }
        return Optional.empty();
    }

    public List<Payment> findByStudent(int studentId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "e.numero_documento AS doc_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, " +
                "a.numero_documento AS doc_acudiente, a.direccion AS dir_acudiente, a.telefono_principal AS tel_acudiente, " +
                "cp.nombre_concepto, cp.codigo_concepto " +
                "FROM pagos p " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto " +
                "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find payments for student " + studentId, e);
        }
        return list;
    }

    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "e.numero_documento AS doc_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, " +
                "a.numero_documento AS doc_acudiente, a.direccion AS dir_acudiente, a.telefono_principal AS tel_acudiente, " +
                "cp.nombre_concepto, cp.codigo_concepto " +
                "FROM pagos p " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto " +
                "ORDER BY p.fecha_pago DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all payments", e);
        }
        return list;
    }

    public List<PaymentConcept> findAllConcepts() {
        List<PaymentConcept> list = new ArrayList<>();
        String sql = "SELECT * FROM conceptos_pago WHERE esta_activo = TRUE ORDER BY id_concepto";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PaymentConcept c = new PaymentConcept();
                c.setId(rs.getInt("id_concepto"));
                c.setCode(rs.getString("codigo_concepto"));
                c.setName(rs.getString("nombre_concepto"));
                c.setDescription(rs.getString("descripcion"));
                c.setBaseAmount(rs.getBigDecimal("monto_base").doubleValue());
                c.setRecurring(rs.getBoolean("es_recurrente"));
                c.setActive(rs.getBoolean("esta_activo"));
                list.add(c);
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to query payment concepts", e);
        }
        return list;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id_pago"));
        p.setStudentId(rs.getInt("id_estudiante"));
        int gId = rs.getInt("id_acudiente");
        if (!rs.wasNull()) p.setGuardianId(gId);
        p.setConceptId(rs.getInt("id_concepto"));
        p.setPaidAmount(rs.getBigDecimal("monto_pagado").doubleValue());
        Date date = rs.getDate("fecha_pago");
        if (date != null) p.setPaymentDate(date.toLocalDate());
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        int year = rs.getInt("año_academico");
        if (!rs.wasNull()) p.setAcademicYear(year);
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));

        p.setStudentName(rs.getString("nom_estudiante"));
        p.setStudentDocNumber(rs.getString("doc_estudiante"));
        p.setGuardianName(rs.getString("nom_acudiente"));
        p.setGuardianDocNumber(rs.getString("doc_acudiente"));
        p.setGuardianAddress(rs.getString("dir_acudiente"));
        p.setGuardianPhone(rs.getString("tel_acudiente"));
        p.setConceptName(rs.getString("nombre_concepto"));
        p.setConceptCode(rs.getString("codigo_concepto"));
        return p;
    }
}
