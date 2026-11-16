package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClubDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, " +
                "horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, club.getName());
            ps.setString(2, club.getDescription());
            if (club.getSupervisorTeacherId() != null) {
                ps.setInt(3, club.getSupervisorTeacherId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getMeetingSchedule());
            ps.setBoolean(6, club.isActive());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        club.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save club: " + club.getName(), e);
        }
        return false;
    }

    public Optional<Club> findById(int id) {
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor, " +
                "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS total_inscritos " +
                "FROM clubes c LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor WHERE c.id_club = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToClub(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find club by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Club> findAll() {
        List<Club> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor, " +
                "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS total_inscritos " +
                "FROM clubes c LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor " +
                "WHERE c.esta_activo = TRUE ORDER BY c.nombre_club";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToClub(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all clubs", e);
        }
        return list;
    }

    public boolean addMember(int studentId, int clubId, LocalDate joinDate) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) " +
                "VALUES (?, ?, ?, 'Activa') ON DUPLICATE KEY UPDATE estado_membresia = 'Activa', fecha_ingreso = VALUES(fecha_ingreso)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, clubId);
            ps.setDate(3, Date.valueOf(joinDate != null ? joinDate : LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.logError("Failed to add member to club " + clubId, e);
        }
        return false;
    }

    public List<ClubMembership> findMembershipsByStudent(int studentId) {
        List<ClubMembership> list = new ArrayList<>();
        String sql = "SELECT mc.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "c.nombre_club FROM membresias_club mc " +
                "INNER JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante " +
                "INNER JOIN clubes c ON mc.id_club = c.id_club " +
                "WHERE mc.id_estudiante = ? AND mc.estado_membresia = 'Activa'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClubMembership m = new ClubMembership();
                    m.setId(rs.getInt("id_membresia"));
                    m.setStudentId(rs.getInt("id_estudiante"));
                    m.setClubId(rs.getInt("id_club"));
                    Date date = rs.getDate("fecha_ingreso");
                    if (date != null) m.setJoinDate(date.toLocalDate());
                    m.setStatus(rs.getString("estado_membresia"));
                    m.setStudentName(rs.getString("nom_estudiante"));
                    m.setClubName(rs.getString("nombre_club"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find club memberships for student " + studentId, e);
        }
        return list;
    }

    private Club mapResultSetToClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        int supervisorId = rs.getInt("id_profesor_supervisor");
        if (!rs.wasNull()) c.setSupervisorTeacherId(supervisorId);
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setSupervisorTeacherName(rs.getString("nom_profesor"));
        c.setActiveMembersCount(rs.getInt("total_inscritos"));
        return c;
    }
}
