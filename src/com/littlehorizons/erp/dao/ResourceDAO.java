package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, " +
                "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, resource.getCode());
            ps.setString(2, resource.getName());
            ps.setInt(3, resource.getCategoryId());
            ps.setString(4, resource.getDescription());
            ps.setDate(5, Date.valueOf(resource.getAcquisitionDate() != null ? resource.getAcquisitionDate() : LocalDate.now()));
            ps.setString(6, resource.getLocation());
            ps.setString(7, resource.getStatus() != null ? resource.getStatus() : "Disponible");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        resource.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save resource: " + resource.getCode(), e);
        }
        return false;
    }

    public Optional<Resource> findById(int id) {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r " +
                "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria WHERE r.id_recurso = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResource(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find resource by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Resource> findByCode(String code) {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r " +
                "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria WHERE r.codigo_recurso = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResource(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find resource by code: " + code, e);
        }
        return Optional.empty();
    }

    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r " +
                "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria ORDER BY r.id_categoria, r.nombre_recurso";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToResource(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all resources", e);
        }
        return list;
    }

    public List<ResourceCategory> findAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM categorias_recurso ORDER BY id_categoria";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ResourceCategory(rs.getInt("id_categoria"), rs.getString("nombre_categoria"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to fetch categories", e);
        }
        return list;
    }

    public boolean loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate returnDate, String obs) {
        String insertSql = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) " +
                "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(insertSql);
                 PreparedStatement ps2 = conn.prepareStatement(updateResourceSql)) {

                ps1.setInt(1, resourceId);
                ps1.setInt(2, studentId);
                ps1.setDate(3, Date.valueOf(loanDate != null ? loanDate : LocalDate.now()));
                ps1.setDate(4, Date.valueOf(returnDate));
                ps1.setString(5, obs);
                ps1.executeUpdate();

                ps2.setInt(1, resourceId);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                LoggerManager.logError("Failed transaction: loan to student", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerManager.logError("Connection error in loanToStudent", e);
        }
        return false;
    }

    public boolean returnStudentLoan(int loanId, LocalDate returnDate) {
        String queryResourceSql = "SELECT id_recurso FROM prestamos_estudiante WHERE id_prestamo = ?";
        String updateLoanSql = "UPDATE prestamos_estudiante SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto' WHERE id_prestamo = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            int resourceId = -1;
            try (PreparedStatement psQ = conn.prepareStatement(queryResourceSql)) {
                psQ.setInt(1, loanId);
                try (ResultSet rs = psQ.executeQuery()) {
                    if (rs.next()) {
                        resourceId = rs.getInt("id_recurso");
                    }
                }
            }
            if (resourceId == -1) return false;

            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(updateLoanSql);
                 PreparedStatement ps2 = conn.prepareStatement(updateResourceSql)) {

                ps1.setDate(1, Date.valueOf(returnDate != null ? returnDate : LocalDate.now()));
                ps1.setInt(2, loanId);
                ps1.executeUpdate();

                ps2.setInt(1, resourceId);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                LoggerManager.logError("Failed transaction: return student loan", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerManager.logError("Connection error in returnStudentLoan", e);
        }
        return false;
    }

    public List<StudentLoan> findOverdueStudentLoans() {
        List<StudentLoan> list = new ArrayList<>();
        String sql = "SELECT pe.*, r.nombre_recurso, r.codigo_recurso, " +
                "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante " +
                "FROM prestamos_estudiante pe " +
                "INNER JOIN recursos r ON pe.id_recurso = r.id_recurso " +
                "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante " +
                "WHERE (pe.estado_prestamo = 'Vencido' OR (pe.estado_prestamo = 'Activo' AND pe.fecha_devolucion_esperada < CURRENT_DATE))";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudentLoan l = new StudentLoan();
                l.setId(rs.getInt("id_prestamo"));
                l.setResourceId(rs.getInt("id_recurso"));
                l.setStudentId(rs.getInt("id_estudiante"));
                Date lDate = rs.getDate("fecha_prestamo");
                if (lDate != null) l.setLoanDate(lDate.toLocalDate());
                Date expDate = rs.getDate("fecha_devolucion_esperada");
                if (expDate != null) l.setExpectedReturnDate(expDate.toLocalDate());
                Date retDate = rs.getDate("fecha_devolucion_real");
                if (retDate != null) l.setActualReturnDate(retDate.toLocalDate());
                l.setStatus(rs.getString("estado_prestamo"));
                l.setObservations(rs.getString("observaciones"));
                l.setResourceName(rs.getString("nombre_recurso"));
                l.setResourceCode(rs.getString("codigo_recurso"));
                l.setStudentName(rs.getString("nom_estudiante"));
                list.add(l);
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to fetch overdue student loans", e);
        }
        return list;
    }

    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id_recurso"));
        r.setCode(rs.getString("codigo_recurso"));
        r.setName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date acq = rs.getDate("fecha_adquisicion");
        if (acq != null) r.setAcquisitionDate(acq.toLocalDate());
        r.setLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(rs.getString("estado_recurso"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }
}
