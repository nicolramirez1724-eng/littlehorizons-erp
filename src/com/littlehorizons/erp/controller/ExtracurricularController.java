package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.service.ExtracurricularService;

import java.util.List;
import java.util.Optional;

public class ExtracurricularController {

    private final ExtracurricularService service;

    public ExtracurricularController() {
        this.service = new ExtracurricularService();
    }

    public List<Club> listAllClubs() {
        return service.getAllActiveClubs();
    }

    public Optional<Club> getClubById(int id) {
        return service.getClubById(id);
    }

    public boolean enrollInClub(int studentId, int clubId) {
        return service.enrollStudentInClub(studentId, clubId);
    }

    public List<ClubMembership> getStudentClubs(int studentId) {
        return service.getStudentClubs(studentId);
    }

    public List<Event> listAllEvents() {
        return service.getAllEvents();
    }

    public Optional<Event> getEventById(int id) {
        return service.getEventById(id);
    }

    public boolean registerEventParticipation(int eventId, int studentId, String role, String position, String observations) {
        return service.registerEventParticipation(eventId, studentId, role, position, observations);
    }

    public List<EventParticipation> getEventParticipants(int eventId) {
        return service.getEventParticipants(eventId);
    }
}
