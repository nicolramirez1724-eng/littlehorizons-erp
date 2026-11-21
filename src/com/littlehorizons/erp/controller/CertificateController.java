package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.service.CertificateService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController() {
        this(new CertificateService());
    }

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    public int getCertificateYear() {
        return CertificateService.CERTIFICATE_YEAR;
    }

    public Optional<Student> findStudent(int studentId) {
        return certificateService.findStudent(studentId);
    }

    public boolean hasAcademicRecords(int studentId) {
        return certificateService.hasAcademicRecords(studentId);
    }

    public File generateCertificate(Student student) throws IOException {
        return certificateService.generateCertificate(student);
    }
}
