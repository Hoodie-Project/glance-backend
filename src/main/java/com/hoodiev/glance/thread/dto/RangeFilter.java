package com.hoodiev.glance.thread.dto;

public enum RangeFilter {
    R_0_5(0.5),
    R_2(2.0),
    R_5(5.0),
    ALL(20000.0);

    private final double km;

    RangeFilter(double km) {
        this.km = km;
    }

    public double getKm() {
        return km;
    }

    public static RangeFilter from(String value) {
        if (value == null || value.isBlank()) {
            return R_5;
        }
        return switch (value.toLowerCase()) {
            case "0.5" -> R_0_5;
            case "2" -> R_2;
            case "5" -> R_5;
            case "all" -> ALL;
            default -> R_5;
        };
    }
}
