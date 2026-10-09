package com.restaurant.queue.modules.dashboard.dto;

/**
 * DTO holding all daily statistics shown on the admin dashboard.
 */
public class DashboardStatsResponse {

    private long customersServedToday;
    private long currentlyWaiting;
    private long currentlyCalled;
    private long skippedToday;
    private long cancelledToday;
    private long avgWaitingTimeMinutes;
    private long availableTables;
    private long occupiedTables;

    // ============================================================
    // Getters & Setters
    // ============================================================

    public long getCustomersServedToday() { return customersServedToday; }
    public void setCustomersServedToday(long customersServedToday) { this.customersServedToday = customersServedToday; }

    public long getCurrentlyWaiting() { return currentlyWaiting; }
    public void setCurrentlyWaiting(long currentlyWaiting) { this.currentlyWaiting = currentlyWaiting; }

    public long getCurrentlyCalled() { return currentlyCalled; }
    public void setCurrentlyCalled(long currentlyCalled) { this.currentlyCalled = currentlyCalled; }

    public long getSkippedToday() { return skippedToday; }
    public void setSkippedToday(long skippedToday) { this.skippedToday = skippedToday; }

    public long getCancelledToday() { return cancelledToday; }
    public void setCancelledToday(long cancelledToday) { this.cancelledToday = cancelledToday; }

    public long getAvgWaitingTimeMinutes() { return avgWaitingTimeMinutes; }
    public void setAvgWaitingTimeMinutes(long avgWaitingTimeMinutes) { this.avgWaitingTimeMinutes = avgWaitingTimeMinutes; }

    public long getAvailableTables() { return availableTables; }
    public void setAvailableTables(long availableTables) { this.availableTables = availableTables; }

    public long getOccupiedTables() { return occupiedTables; }
    public void setOccupiedTables(long occupiedTables) { this.occupiedTables = occupiedTables; }
}
