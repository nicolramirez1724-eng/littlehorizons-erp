package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.service.ResourceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ResourceController {

    private final ResourceService service;

    public ResourceController() {
        this.service = new ResourceService();
    }

    public List<Resource> listAllResources() {
        return service.getAllResources();
    }

    public List<ResourceCategory> listAllCategories() {
        return service.getAllCategories();
    }

    public Optional<Resource> getResourceByCode(String code) {
        return service.getResourceByCode(code);
    }

    public boolean registerResource(String code, String name, int catId, String desc, LocalDate date, String loc) {
        return service.registerResource(code, name, catId, desc, date, loc);
    }

    public boolean loanToStudent(int resourceId, int studentId, LocalDate returnDate, String obs) {
        return service.loanToStudent(resourceId, studentId, returnDate, obs);
    }

    public boolean returnLoan(int loanId) {
        return service.returnStudentLoan(loanId);
    }

    public List<StudentLoan> getOverdueLoans() {
        return service.getOverdueLoans();
    }
}
