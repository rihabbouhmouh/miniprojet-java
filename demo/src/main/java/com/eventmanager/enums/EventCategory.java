package com.eventmanager.enums;

/**
 * Event categories with labels, icons, and colors for UI
 */
public enum EventCategory {
    CONCERT("Concert", "🎵", "#E91E63"),
    THEATRE("Theatre", "🎭", "#9C27B0"),
    CONFERENCE("Conference", "🎤", "#3F51B5"),
    SPORT("Sport", "⚽", "#4CAF50"),
    AUTRE("Other", "🎪", "#FF9800");

    private final String label;
    private final String icon;
    private final String color;

    // Constructor
    EventCategory(String label, String icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    // Getters
    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    /**
     * Get enum from string value
     */
    public static EventCategory fromString(String value) {
        for (EventCategory category : EventCategory.values()) {
            if (category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }

    /**
     * Get display text with icon
     */
    public String getDisplayText() {
        return icon + " " + label;
    }
}