package com.restaurant.queue.modules.customer_queue.dto;

import com.restaurant.queue.common.constants.QueueStatus;

import java.time.LocalDateTime;

/**
 * DTO returned to the customer queue status page.
 */
public class QueueStatusResponse {

    private String tokenNumber;
    private String customerName;
    private Integer partySize;
    private QueueStatus status;
    private long position;          // people ahead (WAITING count before this entry)
    private int estimatedWaitMins;  // position × avgServiceTime
    private LocalDateTime joinTime;
    private String restaurantName;
    private String restaurantSlug;

    // ============================================================
    // Getters & Setters
    // ============================================================

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantSlug() { return restaurantSlug; }
    public void setRestaurantSlug(String restaurantSlug) { this.restaurantSlug = restaurantSlug; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }

    public long getPosition() { return position; }
    public void setPosition(long position) { this.position = position; }

    public int getEstimatedWaitMins() { return estimatedWaitMins; }
    public void setEstimatedWaitMins(int estimatedWaitMins) { this.estimatedWaitMins = estimatedWaitMins; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
}
