package com.restaurant.queue.modules.dashboard.dto;

import java.time.LocalDate;

public class AnalyticsResponseDto {

    private Long restaurantId;
    private String restaurantName;
    private String range;
    private LocalDate startDate;
    private LocalDate endDate;

    // Queue Metrics
    private long totalVisits;
    private long totalGuestsServed;
    private long seatedCount;
    private long waitingCount;
    private long calledCount;
    private long cancelledCount;
    private long skippedCount;

    private double avgWaitMinutes;
    private double medianWaitMinutes;
    private String peakHourLabel;
    private long peakHourCount;
    private int[] hourlyVisits = new int[24];

    // Reservation Metrics
    private long totalReservations;
    private long pendingReservations;
    private long confirmedReservations;
    private long seatedReservations;
    private long cancelledReservations;
    private long noShowReservations;
    private double reservationFulfillmentRate;

    // Table Metrics
    private long availableTables;
    private long occupiedTables;

    public AnalyticsResponseDto() {
    }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public long getTotalVisits() { return totalVisits; }
    public void setTotalVisits(long totalVisits) { this.totalVisits = totalVisits; }

    public long getTotalGuestsServed() { return totalGuestsServed; }
    public void setTotalGuestsServed(long totalGuestsServed) { this.totalGuestsServed = totalGuestsServed; }

    public long getSeatedCount() { return seatedCount; }
    public void setSeatedCount(long seatedCount) { this.seatedCount = seatedCount; }

    public long getWaitingCount() { return waitingCount; }
    public void setWaitingCount(long waitingCount) { this.waitingCount = waitingCount; }

    public long getCalledCount() { return calledCount; }
    public void setCalledCount(long calledCount) { this.calledCount = calledCount; }

    public long getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }

    public long getSkippedCount() { return skippedCount; }
    public void setSkippedCount(long skippedCount) { this.skippedCount = skippedCount; }

    public double getAvgWaitMinutes() { return avgWaitMinutes; }
    public void setAvgWaitMinutes(double avgWaitMinutes) { this.avgWaitMinutes = avgWaitMinutes; }

    public double getMedianWaitMinutes() { return medianWaitMinutes; }
    public void setMedianWaitMinutes(double medianWaitMinutes) { this.medianWaitMinutes = medianWaitMinutes; }

    public String getPeakHourLabel() { return peakHourLabel; }
    public void setPeakHourLabel(String peakHourLabel) { this.peakHourLabel = peakHourLabel; }

    public long getPeakHourCount() { return peakHourCount; }
    public void setPeakHourCount(long peakHourCount) { this.peakHourCount = peakHourCount; }

    public int[] getHourlyVisits() { return hourlyVisits; }
    public void setHourlyVisits(int[] hourlyVisits) { this.hourlyVisits = hourlyVisits; }

    public long getTotalReservations() { return totalReservations; }
    public void setTotalReservations(long totalReservations) { this.totalReservations = totalReservations; }

    public long getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(long pendingReservations) { this.pendingReservations = pendingReservations; }

    public long getConfirmedReservations() { return confirmedReservations; }
    public void setConfirmedReservations(long confirmedReservations) { this.confirmedReservations = confirmedReservations; }

    public long getSeatedReservations() { return seatedReservations; }
    public void setSeatedReservations(long seatedReservations) { this.seatedReservations = seatedReservations; }

    public long getCancelledReservations() { return cancelledReservations; }
    public void setCancelledReservations(long cancelledReservations) { this.cancelledReservations = cancelledReservations; }

    public long getNoShowReservations() { return noShowReservations; }
    public void setNoShowReservations(long noShowReservations) { this.noShowReservations = noShowReservations; }

    public double getReservationFulfillmentRate() { return reservationFulfillmentRate; }
    public void setReservationFulfillmentRate(double reservationFulfillmentRate) { this.reservationFulfillmentRate = reservationFulfillmentRate; }

    public long getAvailableTables() { return availableTables; }
    public void setAvailableTables(long availableTables) { this.availableTables = availableTables; }

    public long getOccupiedTables() { return occupiedTables; }
    public void setOccupiedTables(long occupiedTables) { this.occupiedTables = occupiedTables; }
}
