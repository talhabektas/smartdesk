package com.example.smartdeskbackend.enums;
public enum CustomerSegment {
    VIP("VIP", "VIP Müşteri", 1),
    PREMIUM("PREMIUM", "Premium Müşteri", 2),
    STANDARD("STANDARD", "Standart Müşteri", 3),
    BASIC("BASIC", "Temel Müşteri", 4);

    private final String code;
    private final String displayName;
    private final int priorityLevel;

    CustomerSegment(String code, String displayName, int priorityLevel) {
        this.code = code;
        this.displayName = displayName;
        this.priorityLevel = priorityLevel;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public int getPriorityLevel() { return priorityLevel; }

    public boolean isHighPriority() {
        return this.priorityLevel <= 2;
    }
}