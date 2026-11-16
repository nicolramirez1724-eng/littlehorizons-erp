package com.littlehorizons.erp.model;

public class PaymentConcept {
    private int id;
    private String code;
    private String name;
    private String description;
    private double baseAmount;
    private boolean isRecurring;
    private boolean isActive;

    public PaymentConcept() {}

    public PaymentConcept(int id, String code, String name, String description,
                          double baseAmount, boolean isRecurring, boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.baseAmount = baseAmount;
        this.isRecurring = isRecurring;
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

    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("[%s] %s - $%,.2f COP (%s)", code, name, baseAmount, isRecurring ? "Mensual" : "Pago Único");
    }
}
