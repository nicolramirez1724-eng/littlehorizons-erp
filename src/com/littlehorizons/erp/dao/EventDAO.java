package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean save(Event event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, " +
                "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.hasCompetition());
            if (event.getOrganizerTeacherId() != null) {
                ps.setInt(7, event.getOrganizerTeacherId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        event.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to save event: " + event.getName(), e);
        }
        return false;
    }

    public Optional<Event> findById(int id) {
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor WHERE ev.id_evento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEvent(rs));
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find event by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor " +
                "WHERE ev.esta_activo = TRUE ORDER BY ev.fecha_evento DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to find all events", e);
        }
        return list;
    }

    public boolean addParticipant(int eventId, int studentId, String role, String position, String observations) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE rol_participacion = VALUES(rol_participacion), posicion_obtenida = VALUES(posicion_obtenida)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, studentId);
            ps.setString(3, role != null ? role : "Participante");
            ps.setString(4, position);
            ps.setString(5, observations);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.logError("Failed to add participant to event " + eventId, e);
        }
        return false;
    }

    public List<EventParticipation> findParticipantsByEvent(int eventId) {
        List<EventParticipation> list = new ArrayList<>();
        String sql = "SELECT pe.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "ev.nombre_evento FROM participantes_evento pe " +
                "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante " +
                "INNER JOIN eventos ev ON pe.id_evento = ev.id_evento " +
                "WHERE pe.id_evento = ? ORDER BY pe.posicion_obtenida DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventParticipation ep = new EventParticipation();
                    ep.setId(rs.getInt("id_participacion"));
                    ep.setEventId(rs.getInt("id_evento"));
                    ep.setStudentId(rs.getInt("id_estudiante"));
                    ep.setRole(rs.getString("rol_participacion"));
                    ep.setPosition(rs.getString("posicion_obtenida"));
                    ep.setObservations(rs.getString("observaciones"));
                    ep.setStudentName(rs.getString("nom_estudiante"));
                    ep.setEventName(rs.getString("nombre_evento"));
                    list.add(ep);
                }
            }
        } catch (SQLException e) {
            LoggerManager.logError("Failed to query participants for event " + eventId, e);
        }
        return list;
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event ev = new Event();
        ev.setId(rs.getInt("id_evento"));
        ev.setName(rs.getString("nombre_evento"));
        ev.setDescription(rs.getString("descripcion"));
        ev.setEventType(rs.getString("tipo_evento"));
        Date date = rs.getDate("fecha_evento");
        if (date != null) ev.setEventDate(date.toLocalDate());
        ev.setLocation(rs.getString("lugar"));
        ev.setHasCompetition(rs.getBoolean("tiene_competencia"));
        int orgId = rs.getInt("id_organizador");
        if (!rs.wasNull()) ev.setOrganizerTeacherId(orgId);
        ev.setActive(rs.getBoolean("esta_activo"));
        ev.setOrganizerTeacherName(rs.getString("nom_profesor"));
        return ev;
    }
}
