package com.restaurant.queue.modules.queue_management.dto;

import java.util.List;

public class RestaurantPublicSummaryDto {

    private String restaurantName;
    private String restaurantSlug;
    private long waitingCount;
    private int estimatedWaitMins;
    private String currentlyCalledToken;
    private List<PublicQueueEntryDto> activeEntries;

    public RestaurantPublicSummaryDto() {
    }

    public RestaurantPublicSummaryDto(String restaurantName, String restaurantSlug, long waitingCount,
                                      int estimatedWaitMins, String currentlyCalledToken,
                                      List<PublicQueueEntryDto> activeEntries) {
        this.restaurantName = restaurantName;
        this.restaurantSlug = restaurantSlug;
        this.waitingCount = waitingCount;
        this.estimatedWaitMins = estimatedWaitMins;
        this.currentlyCalledToken = currentlyCalledToken;
        this.activeEntries = activeEntries;
    }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantSlug() { return restaurantSlug; }
    public void setRestaurantSlug(String restaurantSlug) { this.restaurantSlug = restaurantSlug; }

    public long getWaitingCount() { return waitingCount; }
    public void setWaitingCount(long waitingCount) { this.waitingCount = waitingCount; }

    public int getEstimatedWaitMins() { return estimatedWaitMins; }
    public void setEstimatedWaitMins(int estimatedWaitMins) { this.estimatedWaitMins = estimatedWaitMins; }

    public String getCurrentlyCalledToken() { return currentlyCalledToken; }
    public void setCurrentlyCalledToken(String currentlyCalledToken) { this.currentlyCalledToken = currentlyCalledToken; }

    public List<PublicQueueEntryDto> getActiveEntries() { return activeEntries; }
    public void setActiveEntries(List<PublicQueueEntryDto> activeEntries) { this.activeEntries = activeEntries; }

    public static class PublicQueueEntryDto {
        private String tokenNumber;
        private Integer partySize;
        private String status;

        public PublicQueueEntryDto() {}

        public PublicQueueEntryDto(String tokenNumber, Integer partySize, String status) {
            this.tokenNumber = tokenNumber;
            this.partySize = partySize;
            this.status = status;
        }

        public String getTokenNumber() { return tokenNumber; }
        public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

        public Integer getPartySize() { return partySize; }
        public void setPartySize(Integer partySize) { this.partySize = partySize; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
