package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.util.TerminalReader;

import java.util.List;
import java.util.Optional;

public class ExtracurricularView {

    private final ExtracurricularController controller;

    public ExtracurricularView(ExtracurricularController controller) {
        this.controller = controller;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║            MÓDULO: ACTIVIDADES EXTRACURRICULARES             ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Catálogo de Clubes y Cupos Disponibles");
            System.out.println("  2. Inscribir Estudiante en un Club (Validación de Cupo)");
            System.out.println("  3. Consultar Clubes de un Estudiante");
            System.out.println("  4. Calendario de Eventos Institucionales");
            System.out.println("  5. Registrar Participante / Posición en Evento o Concurso");
            System.out.println("  6. Consultar Participantes y Ganadores de un Evento");
            System.out.println("  7. Regresar al Menú Principal");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 7);
            switch (option) {
                case 1:
                    listClubs();
                    break;
                case 2:
                    enrollInClub();
                    break;
                case 3:
                    viewStudentClubs();
                    break;
                case 4:
                    listEvents();
                    break;
                case 5:
                    registerEventParticipant();
                    break;
                case 6:
                    viewEventParticipants();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private void listClubs() {
        System.out.println("\n--- CATÁLOGO DE CLUBES EXTRACURRICULARES ---");
        List<Club> clubs = controller.listAllClubs();
        if (clubs.isEmpty()) {
            System.out.println("No hay clubes activos en este momento.");
        } else {
            System.out.printf("%-4s | %-38s | %-12s | %-24s | %-18s%n",
                    "ID", "NOMBRE DEL CLUB", "INSCRITOS/CUPO", "PROFESOR ENCARGADO", "HORARIO");
            System.out.println("---------------------------------------------------------------------------------------------------------");
            for (Club c : clubs) {
                String cap = c.getActiveMembersCount() + " / " + c.getMaxCapacity();
                System.out.printf("%-4d | %-38s | %-14s | %-24s | %-18s%n",
                        c.getId(), c.getName(), cap,
                        c.getSupervisorTeacherName() != null ? c.getSupervisorTeacherName() : "Por asignar",
                        c.getMeetingSchedule());
            }
        }
        TerminalReader.pause();
    }

    private void enrollInClub() {
        System.out.println("\n--- INSCRIBIR ESTUDIANTE EN CLUB ---");
        int studentId = TerminalReader.readInt("ID del Estudiante", 1, 1000);
        int clubId = TerminalReader.readInt("ID del Club", 1, 100);

        try {
            boolean ok = controller.enrollInClub(studentId, clubId);
            if (ok) {
                System.out.println("  [✓] Estudiante inscrito exitosamente en el club.");
            } else {
                System.out.println("  [✗] No fue posible realizar la inscripción.");
            }
        } catch (Exception e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
        TerminalReader.pause();
    }

    private void viewStudentClubs() {
        System.out.println("\n--- CONSULTAR CLUBES POR ESTUDIANTE ---");
        int studentId = TerminalReader.readInt("ID del Estudiante", 1, 1000);
        List<ClubMembership> memberships = controller.getStudentClubs(studentId);
        if (memberships.isEmpty()) {
            System.out.println("  (El estudiante no está inscrito en ningún club)");
        } else {
            System.out.printf("%-5s | %-35s | %-12s | %-10s%n", "ID", "CLUB", "FECHA INGRESO", "ESTADO");
            System.out.println("--------------------------------------------------------------------");
            for (ClubMembership m : memberships) {
                System.out.printf("%-5d | %-35s | %-12s | %-10s%n",
                        m.getId(), m.getClubName(), m.getJoinDate(), m.getStatus());
            }
        }
        TerminalReader.pause();
    }

    private void listEvents() {
        System.out.println("\n--- CALENDARIO DE EVENTOS INSTITUCIONALES ---");
        List<Event> events = controller.listAllEvents();
        if (events.isEmpty()) {
            System.out.println("No hay eventos programados.");
        } else {
            System.out.printf("%-4s | %-12s | %-40s | %-12s | %-14s%n",
                    "ID", "FECHA", "EVENTO", "TIPO", "COMPETENCIA");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (Event e : events) {
                System.out.printf("%-4d | %-12s | %-40s | %-12s | %-14s%n",
                        e.getId(), e.getEventDate(), e.getName(), e.getEventType(), e.hasCompetition() ? "SÍ" : "NO");
            }
        }
        TerminalReader.pause();
    }

    private void registerEventParticipant() {
        System.out.println("\n--- REGISTRAR PARTICIPACIÓN / PREMIACIÓN ---");
        int eventId = TerminalReader.readInt("ID del Evento", 1, 100);
        int studentId = TerminalReader.readInt("ID del Estudiante", 1, 1000);

        System.out.println("Roles de participación:");
        System.out.println(" 1. Participante  |  2. Competidor  |  3. Organizador  |  4. Asistente");
        int rOpt = TerminalReader.readInt("Seleccione", 1, 4);
        String[] roles = {"Participante", "Competidor", "Organizador", "Asistente"};
        String role = roles[rOpt - 1];

        String position = null;
        if (role.equals("Competidor")) {
            System.out.println("Posición alcanzada (opciones: 1er Lugar, 2do Lugar, 3er Lugar, Participante):");
            position = TerminalReader.readOptionalString("Posición");
        }

        String obs = TerminalReader.readOptionalString("Observaciones sobre el proyecto o desempeño");

        boolean ok = controller.registerEventParticipation(eventId, studentId, role, position, obs);
        if (ok) {
            System.out.println("  [✓] Participación registrada con éxito.");
        } else {
            System.out.println("  [✗] Error al registrar participación.");
        }
        TerminalReader.pause();
    }

    private void viewEventParticipants() {
        System.out.println("\n--- PARTICIPANTES Y GANADORES DEL EVENTO ---");
        int eventId = TerminalReader.readInt("ID del Evento", 1, 100);
        List<EventParticipation> parts = controller.getEventParticipants(eventId);
        if (parts.isEmpty()) {
            System.out.println("  (No hay participantes registrados en este evento)");
        } else {
            System.out.printf("%-28s | %-14s | %-14s | %-30s%n",
                    "ESTUDIANTE", "ROL", "POSICIÓN", "OBSERVACIÓN / PROYECTO");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (EventParticipation p : parts) {
                System.out.printf("%-28s | %-14s | %-14s | %-30s%n",
                        p.getStudentName(), p.getRole(),
                        p.getPosition() != null ? p.getPosition() : "-",
                        p.getObservations() != null ? p.getObservations() : "");
            }
        }
        TerminalReader.pause();
    }
}
