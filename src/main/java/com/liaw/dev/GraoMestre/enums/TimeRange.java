package com.liaw.dev.GraoMestre.enums;

public enum TimeRange {
    FIFTEEN_MINUTES("15 minutos"),
    THIRTY_MINUTES("30 minutos"),
    ONE_HOUR("1 hora"),
    TWO_HOURS("2 horas");

    private final String description;

    TimeRange(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}