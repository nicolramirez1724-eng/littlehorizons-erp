package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.AttendanceDAO;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing daily attendance and absenteeism alerts.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public boolean recordAttendance(int studentId, int subjectId, LocalDate date,
                                    String status, String observations, Integer registeredBy) {
        if (!InputValidator.isValidAttendanceStatus(status)) {
            throw new IllegalArgumentException("Estado de asistencia inválido (Presente, Ausente, Tardanza, Excusa Médica).");
        }
        Attendance a = new Attendance(0, studentId, subjectId, date != null ? date : LocalDate.now(),
                status, observations, registeredBy);
        return attendanceDAO.save(a);
    }

    public List<Attendance> getStudentAttendanceHistory(int studentId) {
        return attendanceDAO.findByStudent(studentId);
    }

    /**
     * Identifies concerning absence patterns (e.g. more than 3 absences in a week).
     */
    public boolean hasChronicWeeklyAbsences(int studentId, LocalDate referenceDate) {
        LocalDate startOfWeek = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Attendance> absences = attendanceDAO.findAbsencesByStudent(studentId, startOfWeek, endOfWeek);
        long count = absences.stream()
                .filter(a -> "Ausente".equalsIgnoreCase(a.getStatus()))
                .count();
        return count >= 3;
    }

    /**
     * Returns all absences and tardiness records for a student within a date window.
     */
    public List<Attendance> getAbsencesInPeriod(int studentId, LocalDate start, LocalDate end) {
        return attendanceDAO.findAbsencesByStudent(studentId, start, end);
    }
}
