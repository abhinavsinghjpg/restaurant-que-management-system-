package com.restaurant.queue.modules.queue_management.dto;

import java.util.List;

public class StaffLiveQueueDto {

    private Long restaurantId;
    private String restaurantName;
    private long waitingCount;
    private long calledCount;
    private StaffQueueItemDto currentlyCalled;
    private List<StaffQueueItemDto> entries;
    private List<StaffTableItemDto> availableTables;
    private long timestamp;

    public StaffLiveQueueDto() {
        this.timestamp = System.currentTimeMillis();
    }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public long getWaitingCount() { return waitingCount; }
    public void setWaitingCount(long waitingCount) { this.waitingCount = waitingCount; }

    public long getCalledCount() { return calledCount; }
    public void setCalledCount(long calledCount) { this.calledCount = calledCount; }

    public StaffQueueItemDto getCurrentlyCalled() { return currentlyCalled; }
    public void setCurrentlyCalled(StaffQueueItemDto currentlyCalled) { this.currentlyCalled = currentlyCalled; }

    public List<StaffQueueItemDto> getEntries() { return entries; }
    public void setEntries(List<StaffQueueItemDto> entries) { this.entries = entries; }

    public List<StaffTableItemDto> getAvailableTables() { return availableTables; }
    public void setAvailableTables(List<StaffTableItemDto> availableTables) { this.availableTables = availableTables; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public static class StaffQueueItemDto {
        private Long id;
        private String tokenNumber;
        private String customerName;
        private Integer partySize;
        private String status;
        private String joinTimeFormatted;

        public StaffQueueItemDto() {}

        public StaffQueueItemDto(Long id, String tokenNumber, String customerName, Integer partySize, String status, String joinTimeFormatted) {
            this.id = id;
            this.tokenNumber = tokenNumber;
            this.customerName = customerName;
            this.partySize = partySize;
            this.status = status;
            this.joinTimeFormatted = joinTimeFormatted;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTokenNumber() { return tokenNumber; }
        public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public Integer getPartySize() { return partySize; }
        public void setPartySize(Integer partySize) { this.partySize = partySize; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getJoinTimeFormatted() { return joinTimeFormatted; }
        public void setJoinTimeFormatted(String joinTimeFormatted) { this.joinTimeFormatted = joinTimeFormatted; }
    }

    public static class StaffTableItemDto {
        private Long id;
        private String tableNumber;
        private Integer capacity;

        public StaffTableItemDto() {}

        public StaffTableItemDto(Long id, String tableNumber, Integer capacity) {
            this.id = id;
            this.tableNumber = tableNumber;
            this.capacity = capacity;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTableNumber() { return tableNumber; }
        public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
    }
}
