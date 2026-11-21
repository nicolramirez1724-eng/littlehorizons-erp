package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CertificateData(Student student, String gradeName, int year,
                              List<CertificateSubjectLine> subjects, BigDecimal generalAverage,
                              LocalDate issueDate) {
}
