package com.restaurant.queue.modules.queue_management.controller;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.ResourceNotFoundException;
import com.restaurant.queue.config.AppConfig;
import com.restaurant.queue.modules.authentication.service.AuthService;
import com.restaurant.queue.modules.customer_queue.dto.QueueStatusResponse;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import com.restaurant.queue.modules.queue_management.dto.RestaurantPublicSummaryDto;
import com.restaurant.queue.modules.queue_management.dto.StaffLiveQueueDto;
import com.restaurant.queue.modules.queue_management.service.QueueManagementService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QueueApiController {

    private final CustomerQueueService customerQueueService;
    private final QueueManagementService queueManagementService;
    private final RestaurantRepository restaurantRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final TableManagementService tableManagementService;
    private final AuthService authService;
    private final AppConfig appConfig;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public QueueApiController(CustomerQueueService customerQueueService,
                              QueueManagementService queueManagementService,
                              RestaurantRepository restaurantRepository,
                              QueueEntryRepository queueEntryRepository,
                              TableManagementService tableManagementService,
                              AuthService authService,
                              AppConfig appConfig) {
        this.customerQueueService = customerQueueService;
        this.queueManagementService = queueManagementService;
        this.restaurantRepository = restaurantRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.tableManagementService = tableManagementService;
        this.authService = authService;
        this.appConfig = appConfig;
    }

    /**
     * Public endpoint for customer live token tracking.
     */
    @GetMapping("/api/queue/status/{tokenNumber}")
    public ResponseEntity<QueueStatusResponse> getCustomerTokenStatus(@PathVariable String tokenNumber) {
        QueueStatusResponse status = customerQueueService.getQueueStatus(tokenNumber);
        return ResponseEntity.ok(status);
    }

    /**
     * Public endpoint for restaurant QR landing page summary (anonymized for privacy).
     */
    @GetMapping("/api/r/{slug}/summary")
    public ResponseEntity<RestaurantPublicSummaryDto> getRestaurantQueueSummary(@PathVariable String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + slug));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            return ResponseEntity.notFound().build();
        }

        long waitingCount = queueEntryRepository.countByRestaurantIdAndStatus(restaurant.getId(), QueueStatus.WAITING);
        List<QueueEntry> calledList = queueEntryRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurant.getId(), QueueStatus.CALLED);
        String currentlyCalled = calledList.isEmpty() ? null : calledList.get(0).getTokenNumber();
        int estWait = (int) (waitingCount * appConfig.getAvgServiceTimeMinutes());

        List<QueueEntry> activeQueue = queueEntryRepository.findActiveQueueByRestaurantId(restaurant.getId());
        List<RestaurantPublicSummaryDto.PublicQueueEntryDto> anonymized = activeQueue.stream()
                .map(q -> new RestaurantPublicSummaryDto.PublicQueueEntryDto(
                        q.getTokenNumber(),
                        q.getPartySize(),
                        q.getStatus().name()
                ))
                .collect(Collectors.toList());

        RestaurantPublicSummaryDto summary = new RestaurantPublicSummaryDto(
                restaurant.getName(),
                restaurant.getSlug(),
                waitingCount,
                estWait,
                currentlyCalled,
                anonymized
        );

        return ResponseEntity.ok(summary);
    }

    /**
     * Authenticated endpoint for staff/admin live queue polling.
     */
    @GetMapping("/api/admin/queue/data")
    public ResponseEntity<StaffLiveQueueDto> getStaffLiveQueue(
            @RequestParam(required = false) Long restaurantId) {
        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(resolvedId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + resolvedId));

        List<QueueEntry> activeList = queueManagementService.getActiveQueue(resolvedId);
        List<RestaurantTable> tables = tableManagementService.getAllTables(resolvedId);

        StaffLiveQueueDto dto = new StaffLiveQueueDto();
        dto.setRestaurantId(restaurant.getId());
        dto.setRestaurantName(restaurant.getName());

        long waitingCount = activeList.stream()
                .filter(q -> q.getStatus() == QueueStatus.WAITING).count();
        long calledCount = activeList.stream()
                .filter(q -> q.getStatus() == QueueStatus.CALLED).count();
        dto.setWaitingCount(waitingCount);
        dto.setCalledCount(calledCount);

        // Currently called
        activeList.stream()
                .filter(q -> q.getStatus() == QueueStatus.CALLED)
                .findFirst()
                .ifPresent(c -> dto.setCurrentlyCalled(new StaffLiveQueueDto.StaffQueueItemDto(
                        c.getId(),
                        c.getTokenNumber(),
                        c.getCustomerName(),
                        c.getPartySize(),
                        c.getStatus().name(),
                        c.getJoinTime() != null ? c.getJoinTime().format(TIME_FMT) : ""
                )));

        // Active queue entries
        List<StaffLiveQueueDto.StaffQueueItemDto> entries = activeList.stream()
                .map(e -> new StaffLiveQueueDto.StaffQueueItemDto(
                        e.getId(),
                        e.getTokenNumber(),
                        e.getCustomerName(),
                        e.getPartySize(),
                        e.getStatus().name(),
                        e.getJoinTime() != null ? e.getJoinTime().format(TIME_FMT) : ""
                ))
                .collect(Collectors.toList());
        dto.setEntries(entries);

        // Available tables
        List<StaffLiveQueueDto.StaffTableItemDto> availTables = tables.stream()
                .filter(t -> t.getStatus() == TableStatus.AVAILABLE)
                .map(t -> new StaffLiveQueueDto.StaffTableItemDto(t.getId(), t.getTableNumber(), t.getCapacity()))
                .collect(Collectors.toList());
        dto.setAvailableTables(availTables);

        return ResponseEntity.ok(dto);
    }
}
