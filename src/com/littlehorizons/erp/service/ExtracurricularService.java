package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.ClubDAO;
import com.littlehorizons.erp.dao.EventDAO;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing extracurricular clubs and institutional events.
 */
public class ExtracurricularService {

    private final ClubDAO clubDAO;
    private final EventDAO eventDAO;

    public ExtracurricularService() {
        this.clubDAO = new ClubDAO();
        this.eventDAO = new EventDAO();
    }

    public ExtracurricularService(ClubDAO clubDAO, EventDAO eventDAO) {
        this.clubDAO = clubDAO;
        this.eventDAO = eventDAO;
    }

    public List<Club> getAllActiveClubs() {
        return clubDAO.findAll();
    }

    public Optional<Club> getClubById(int clubId) {
        return clubDAO.findById(clubId);
    }

    /**
     * Enrolls a student in a club verifying that capacity is not exceeded.
     */
    public boolean enrollStudentInClub(int studentId, int clubId) {
        Optional<Club> clubOpt = clubDAO.findById(clubId);
        if (!clubOpt.isPresent()) {
            throw new IllegalArgumentException("El club seleccionado no existe.");
        }
        Club club = clubOpt.get();
        if (club.getActiveMembersCount() >= club.getMaxCapacity()) {
            throw new IllegalStateException("El club '" + club.getName() + "' ha alcanzado su cupo máximo de " + club.getMaxCapacity() + " estudiantes.");
        }
        return clubDAO.addMember(studentId, clubId, LocalDate.now());
    }

    public List<ClubMembership> getStudentClubs(int studentId) {
        return clubDAO.findMembershipsByStudent(studentId);
    }

    public List<Event> getAllEvents() {
        return eventDAO.findAll();
    }

    public Optional<Event> getEventById(int eventId) {
        return eventDAO.findById(eventId);
    }

    public boolean registerEventParticipation(int eventId, int studentId, String role, String position, String observations) {
        return eventDAO.addParticipant(eventId, studentId, role, position, observations);
    }

    public List<EventParticipation> getEventParticipants(int eventId) {
        return eventDAO.findParticipantsByEvent(eventId);
    }
}
