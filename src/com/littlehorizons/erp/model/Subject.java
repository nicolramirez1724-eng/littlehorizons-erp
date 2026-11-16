package com.littlehorizons.erp.model;

public class Subject {
    private int id;
    private String code;
    private String name;
    private String description;
    private int gradeId;
    private int weeklyHours;
    private boolean isActive;

    public Subject() {}

    public Subject(int id, String code, String name, String description, int gradeId, int weeklyHours, boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.gradeId = gradeId;
        this.weeklyHours = weeklyHours;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d hrs/sem)", code, name, weeklyHours);
    }
}
