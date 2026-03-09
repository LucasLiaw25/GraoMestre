package com.liaw.dev.GraoMestre.enums;

public enum TimePeriod {
    TODAY("Hoje"),
    YESTERDAY("Ontem"),
    THIS_WEEK("Esta Semana"),
    LAST_WEEK("Semana Passada"),
    THIS_MONTH("Este Mês"),
    LAST_MONTH("Mês Passado"),
    CUSTOM("Personalizado");

    private final String description;

    TimePeriod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}