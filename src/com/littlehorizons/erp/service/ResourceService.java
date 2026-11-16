package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.StudentLoan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing library, laboratory, and sports resources and loan lifecycles.
 */
public class ResourceService {

    private final ResourceDAO resourceDAO;

    public ResourceService() {
        this.resourceDAO = new ResourceDAO();
    }

    public ResourceService(ResourceDAO resourceDAO) {
        this.resourceDAO = resourceDAO;
    }

    public List<Resource> getAllResources() {
        return resourceDAO.findAll();
    }

    public List<ResourceCategory> getAllCategories() {
        return resourceDAO.findAllCategories();
    }

    public Optional<Resource> getResourceByCode(String code) {
        return resourceDAO.findByCode(code);
    }

    public boolean registerResource(String code, String name, int categoryId, String description,
                                    LocalDate acquisitionDate, String location) {
        Resource r = new Resource(0, code, name, categoryId, description, acquisitionDate, location, "Disponible");
        return resourceDAO.save(r);
    }

    /**
     * Loans a resource to a student verifying availability.
     */
    public boolean loanToStudent(int resourceId, int studentId, LocalDate returnDate, String observations) {
        Optional<Resource> res = resourceDAO.findById(resourceId);
        if (!res.isPresent()) {
            throw new IllegalArgumentException("El recurso especificado no existe.");
        }
        if (!"Disponible".equalsIgnoreCase(res.get().getStatus())) {
            throw new IllegalStateException("El recurso no se encuentra disponible (Estado actual: " + res.get().getStatus() + ").");
        }
        if (returnDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución esperada no puede ser anterior a hoy.");
        }
        return resourceDAO.loanToStudent(resourceId, studentId, LocalDate.now(), returnDate, observations);
    }

    public boolean returnStudentLoan(int loanId) {
        return resourceDAO.returnStudentLoan(loanId, LocalDate.now());
    }

    public List<StudentLoan> getOverdueLoans() {
        return resourceDAO.findOverdueStudentLoans();
    }
}
