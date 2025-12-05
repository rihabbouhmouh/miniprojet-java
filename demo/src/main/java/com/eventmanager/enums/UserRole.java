package com.eventmanager.enums;

public enum UserRole {
    ADMIN,
    ORGANIZER,
    CLIENT;

    public String getLabel() {
        return switch (this) {
            case ADMIN -> "Administrateur";
            case ORGANIZER -> "Organisateur";
            case CLIENT -> "Client";
        };
    }

    public String getColor() {
        return switch (this) {
            case ADMIN -> "#ff4444";
            case ORGANIZER -> "#ff9900";
            case CLIENT -> "#33b5e5";
        };
    }
}