package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.CertificateDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.CertificateData;
import com.littlehorizons.erp.model.CertificateGrade;
import com.littlehorizons.erp.model.CertificateSubjectLine;
import com.littlehorizons.erp.model.Student;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CertificateService {

    public static final int CERTIFICATE_YEAR = 2025;

    private static final String NO_ENROLLMENT_LABEL = "Sin matrícula " + CERTIFICATE_YEAR;

    private final StudentDAO studentDAO;
    private final CertificateDAO certificateDAO;

    public CertificateService() {
        this(new StudentDAO(), new CertificateDAO());
    }

    public CertificateService(StudentDAO studentDAO, CertificateDAO certificateDAO) {
        this.studentDAO = studentDAO;
        this.certificateDAO = certificateDAO;
    }

    public Optional<Student> findStudent(int studentId) {
        return studentDAO.findById(studentId);
    }

    public boolean hasAcademicRecords(int studentId) {
        return !certificateDAO.findGrades(studentId, CERTIFICATE_YEAR).isEmpty();
    }

    public File generateCertificate(Student student) throws IOException {
        CertificateData data = buildCertificateData(student);
        String content = CertificateFormatter.format(data);
        File file = new File(String.format("certificado_%d_%d.txt", student.getId(), CERTIFICATE_YEAR));
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        return file.getAbsoluteFile();
    }

    private CertificateData buildCertificateData(Student student) {
        String gradeName = certificateDAO.findEnrollmentGradeName(student.getId(), CERTIFICATE_YEAR)
                .orElse(NO_ENROLLMENT_LABEL);
        List<CertificateGrade> grades = certificateDAO.findGrades(student.getId(), CERTIFICATE_YEAR);

        Map<Integer, List<CertificateGrade>> gradesBySubject = grades.stream()
                .collect(Collectors.groupingBy(CertificateGrade::subjectId, LinkedHashMap::new, Collectors.toList()));

        List<CertificateSubjectLine> subjects = gradesBySubject.values().stream()
                .map(this::buildSubjectLine)
                .collect(Collectors.toList());

        BigDecimal generalAverage = average(subjects.stream()
                .map(CertificateSubjectLine::average)
                .collect(Collectors.toList()));

        return new CertificateData(student, gradeName, CERTIFICATE_YEAR, subjects, generalAverage, LocalDate.now());
    }

    private CertificateSubjectLine buildSubjectLine(List<CertificateGrade> subjectGrades) {
        CertificateGrade first = subjectGrades.get(0);
        Map<String, BigDecimal> gradesByPeriod = new LinkedHashMap<>();
        for (CertificateGrade grade : subjectGrades) {
            gradesByPeriod.put(grade.period(), grade.value());
        }
        BigDecimal subjectAverage = average(List.copyOf(gradesByPeriod.values()));
        return new CertificateSubjectLine(first.subjectCode(), first.subjectName(), gradesByPeriod, subjectAverage);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(1);
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }
}
