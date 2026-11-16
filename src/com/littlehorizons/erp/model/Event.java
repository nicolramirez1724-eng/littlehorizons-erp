package com.littlehorizons.erp.model;

import java.time.LocalDate;

public class Event {
    private int id;
    private String name;
    private String description;
    private String eventType; // Académico, Cultural, Deportivo, Social, Científico
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private Integer organizerTeacherId;
    private boolean isActive;

    private String organizerTeacherName;

    public Event() {}

    public Event(int id, String name, String description, String eventType, LocalDate eventDate,
                 String location, boolean hasCompetition, Integer organizerTeacherId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
        this.organizerTeacherId = organizerTeacherId;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean hasCompetition() { return hasCompetition; }
    public void setHasCompetition(boolean hasCompetition) { this.hasCompetition = hasCompetition; }

    public Integer getOrganizerTeacherId() { return organizerTeacherId; }
    public void setOrganizerTeacherId(Integer organizerTeacherId) { this.organizerTeacherId = organizerTeacherId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getOrganizerTeacherName() { return organizerTeacherName; }
    public void setOrganizerTeacherName(String organizerTeacherName) { this.organizerTeacherName = organizerTeacherName; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) en %s", eventDate, name, eventType, location);
    }
}
