package com.restaurant.queue;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.dashboard.dto.AnalyticsResponseDto;
import com.restaurant.queue.modules.dashboard.service.AnalyticsService;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.repository.ReservationRepository;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Restaurant r1;
    private Restaurant r2;

    @BeforeEach
    void setUp() {
        r1 = restaurantRepository.findBySlug("default-restaurant").orElseGet(() -> {
            Restaurant r = new Restaurant("default-restaurant", "Main Bistro");
            r.setActive(true);
            return restaurantRepository.save(r);
        });

        r2 = restaurantRepository.findBySlug("second-bistro").orElseGet(() -> {
            Restaurant r = new Restaurant("second-bistro", "Second Bistro");
            r.setActive(true);
            return restaurantRepository.save(r);
        });
    }

    @Test
    void testAnalyticsCalculationsAndMetrics() {
        LocalDateTime now = java.time.LocalDate.now().atTime(12, 30);

        long uid = System.nanoTime();

        // Seed 1 seated entry with 20 minutes wait time
        QueueEntry seatedEntry = new QueueEntry();
        seatedEntry.setRestaurantId(r1.getId());
        seatedEntry.setTokenNumber("TEST-S-" + uid);
        seatedEntry.setCustomerName("Analytics Customer 1");
        seatedEntry.setMobile("9888811111");
        seatedEntry.setPartySize(4);
        seatedEntry.setStatus(QueueStatus.SEATED);
        seatedEntry.setJoinTime(now.minusMinutes(20));
        seatedEntry.setSeatedTime(now);
        queueEntryRepository.save(seatedEntry);

        // Seed 1 cancelled entry
        QueueEntry cancelledEntry = new QueueEntry();
        cancelledEntry.setRestaurantId(r1.getId());
        cancelledEntry.setTokenNumber("TEST-C-" + uid);
        cancelledEntry.setCustomerName("Analytics Customer 2");
        cancelledEntry.setMobile("9888822222");
        cancelledEntry.setPartySize(2);
        cancelledEntry.setStatus(QueueStatus.CANCELLED);
        cancelledEntry.setJoinTime(now.minusMinutes(10));
        queueEntryRepository.save(cancelledEntry);

        // Seed 1 seated reservation and 1 cancelled reservation (guaranteed within today)
        Reservation resSeated = new Reservation(r1.getId(), "Res Customer 1", "9888833333", 4, now.withHour(10).withMinute(0));
        resSeated.setStatus(ReservationStatus.SEATED);
        reservationRepository.save(resSeated);

        Reservation resCancelled = new Reservation(r1.getId(), "Res Customer 2", "9888844444", 2, now.withHour(11).withMinute(0));
        resCancelled.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(resCancelled);

        AnalyticsResponseDto result = analyticsService.getAnalytics(r1.getId(), "today", null, null);

        assertNotNull(result);
        assertEquals(r1.getId(), result.getRestaurantId());
        assertTrue(result.getTotalVisits() >= 2);
        assertTrue(result.getSeatedCount() >= 1);
        assertTrue(result.getCancelledCount() >= 1);
        assertTrue(result.getTotalGuestsServed() >= 4);
        assertTrue(result.getAvgWaitMinutes() >= 15.0); // 20 mins wait
        assertNotNull(result.getPeakHourLabel());
        assertTrue(result.getTotalReservations() >= 2);
        assertTrue(result.getSeatedReservations() >= 1);
        assertTrue(result.getReservationFulfillmentRate() >= 40.0);
    }

    @Test
    void testAnalyticsMultiRestaurantIsolation() {
        // Create an entry in R2
        QueueEntry r2Entry = new QueueEntry();
        r2Entry.setRestaurantId(r2.getId());
        r2Entry.setTokenNumber("TEST-R2-" + System.nanoTime());
        r2Entry.setCustomerName("R2 Exclusive Customer");
        r2Entry.setMobile("9999900000");
        r2Entry.setPartySize(8);
        r2Entry.setStatus(QueueStatus.SEATED);
        r2Entry.setJoinTime(LocalDateTime.now().minusMinutes(5));
        r2Entry.setSeatedTime(LocalDateTime.now());
        queueEntryRepository.save(r2Entry);

        AnalyticsResponseDto r1Analytics = analyticsService.getAnalytics(r1.getId(), "today", null, null);
        AnalyticsResponseDto r2Analytics = analyticsService.getAnalytics(r2.getId(), "today", null, null);

        assertNotEquals(r1Analytics.getRestaurantId(), r2Analytics.getRestaurantId());
        assertTrue(r2Analytics.getTotalVisits() >= 1);
    }

    @Test
    void testEmptyAnalyticsDoesNotThrow() {
        AnalyticsResponseDto empty = analyticsService.getAnalytics(r1.getId(), "custom",
                LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 2));

        assertNotNull(empty);
        assertEquals(0, empty.getTotalVisits());
        assertEquals(0.0, empty.getAvgWaitMinutes());
        assertEquals(0.0, empty.getReservationFulfillmentRate());
        assertEquals("No Traffic", empty.getPeakHourLabel());
    }

    @Test
    void testAnalyticsUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/admin/analytics"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminAnalyticsPageMvc() throws Exception {
        mockMvc.perform(get("/admin/analytics")
                        .param("restaurantId", String.valueOf(r1.getId()))
                        .param("range", "7days"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/analytics"))
                .andExpect(model().attributeExists("analytics"))
                .andExpect(model().attribute("activeRange", "7days"));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testStaffAnalyticsApiEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/data")
                        .param("restaurantId", String.valueOf(r1.getId()))
                        .param("range", "today"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.restaurantId", is(r1.getId().intValue())))
                .andExpect(jsonPath("$.hourlyVisits", notNullValue()));
    }
}
