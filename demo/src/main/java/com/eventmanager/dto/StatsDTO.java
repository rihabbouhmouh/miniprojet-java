package com.eventmanager.dto;

import java.util.Map;

public class StatsDTO {

    private Map<String, Object> userStats;
    private Map<String, Object> eventStats;
    private Map<String, Object> reservationStats;
    private Map<String, Object> financialStats;

    public StatsDTO() {}

    public StatsDTO(Map<String, Object> userStats, Map<String, Object> eventStats,
                    Map<String, Object> reservationStats, Map<String, Object> financialStats) {
        this.userStats = userStats;
        this.eventStats = eventStats;
        this.reservationStats = reservationStats;
        this.financialStats = financialStats;
    }

    // Getters & Setters
    public Map<String, Object> getUserStats() { return userStats; }
    public void setUserStats(Map<String, Object> userStats) { this.userStats = userStats; }

    public Map<String, Object> getEventStats() { return eventStats; }
    public void setEventStats(Map<String, Object> eventStats) { this.eventStats = eventStats; }

    public Map<String, Object> getReservationStats() { return reservationStats; }
    public void setReservationStats(Map<String, Object> reservationStats) { this.reservationStats = reservationStats; }

    public Map<String, Object> getFinancialStats() { return financialStats; }
    public void setFinancialStats(Map<String, Object> financialStats) { this.financialStats = financialStats; }
}