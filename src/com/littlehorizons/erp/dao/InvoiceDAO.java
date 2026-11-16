package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public boolean save(Invoice invoice) {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE cufe = VALUES(cufe), nombre_archivo_pdf = VALUES(nombre_archivo_pdf), ruta_archivo = VALUES(ruta_archivo)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, invoice.getInvoiceNumber());
            ps.setInt(2, invoice.getPaymentId());
            ps.setDate(3, Date.valueOf(invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now()));
            ps.setString(4, invoice.getCufe());
            ps.setString(5, invoice.getPdfFileName());
            ps.setString(6, invoice.getFilePath());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save invoice: " + invoice.getInvoiceNumber(), e);
        }
        return false;
    }

    public Optional<Invoice> findById(int id) {
        String sql = "SELECT * FROM facturas WHERE id_factura = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    paymentDAO.findById(inv.getPaymentId()).ifPresent(inv::setPayment);
                    return Optional.of(inv);
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find invoice by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Invoice> findByPaymentId(int paymentId) {
        String sql = "SELECT * FROM facturas WHERE id_pago = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    paymentDAO.findById(inv.getPaymentId()).ifPresent(inv::setPayment);
                    return Optional.of(inv);
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find invoice for payment: " + paymentId, e);
        }
        return Optional.empty();
    }

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM facturas ORDER BY fecha_emision DESC, id_factura DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Invoice inv = mapResultSetToInvoice(rs);
                paymentDAO.findById(inv.getPaymentId()).ifPresent(inv::setPayment);
                list.add(inv);
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to fetch all invoices", e);
        }
        return list;
    }

    /**
     * Generates next consecutive electronic invoice number (e.g. FE-000133).
     */
    public String getNextInvoiceNumber() {
        String sql = "SELECT numero_factura FROM facturas ORDER BY id_factura DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastNum = rs.getString("numero_factura");
                if (lastNum != null && lastNum.startsWith("FE-")) {
                    try {
                        int num = Integer.parseInt(lastNum.substring(3));
                        return String.format("FE-%06d", num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to determine next invoice number", e);
        }
        return "FE-000101";
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id_factura"));
        inv.setInvoiceNumber(rs.getString("numero_factura"));
        inv.setPaymentId(rs.getInt("id_pago"));
        Date date = rs.getDate("fecha_emision");
        if (date != null) inv.setIssueDate(date.toLocalDate());
        inv.setCufe(rs.getString("cufe"));
        inv.setPdfFileName(rs.getString("nombre_archivo_pdf"));
        inv.setFilePath(rs.getString("ruta_archivo"));
        return inv;
    }
}
