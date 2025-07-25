package com.example.smartdeskbackend.enums;

public enum TicketPriority {
    LOW("LOW", "Düşük", 1, "#28a745"),
    NORMAL("NORMAL", "Normal", 2, "#17a2b8"),
    HIGH("HIGH", "Yüksek", 3, "#fd7e14"),
    URGENT("URGENT", "Acil", 4, "#dc3545"),
    CRITICAL("CRITICAL", "Kritik", 5, "#6f42c1");

    private final String code;
    private final String displayName;
    private final int level;
    private final String color;

    TicketPriority(String code, String displayName, int level, String color) {
        this.code = code;
        this.displayName = displayName;
        this.level = level;
        this.color = color;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    public String getColor() { return color; }

    public boolean isHighPriority() {
        return this.level >= HIGH.level;
    }
}