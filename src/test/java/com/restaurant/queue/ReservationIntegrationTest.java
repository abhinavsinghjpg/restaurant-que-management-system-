package com.restaurant.queue;

import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.modules.reservations.dto.CreateReservationRequest;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.repository.ReservationRepository;
import com.restaurant.queue.modules.reservations.service.ReservationService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    private Restaurant r1;
    private Restaurant r2;
    private RestaurantTable tableR1Small;
    private RestaurantTable tableR1Large;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();

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

        tableR1Small = tableRepository.findByTableNumberAndRestaurantId("T-RES-2", r1.getId())
                .orElseGet(() -> {
                    RestaurantTable t = new RestaurantTable("T-RES-2", 2, TableStatus.AVAILABLE);
                    t.setRestaurantId(r1.getId());
                    return tableRepository.save(t);
                });

        tableR1Large = tableRepository.findByTableNumberAndRestaurantId("T-RES-6", r1.getId())
                .orElseGet(() -> {
                    RestaurantTable t = new RestaurantTable("T-RES-6", 6, TableStatus.AVAILABLE);
                    t.setRestaurantId(r1.getId());
                    return tableRepository.save(t);
                });
    }

    @Test
    void testCreateReservationSuccess() {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setCustomerName("Alice Smith");
        req.setMobile("9876543210");
        req.setPartySize(4);
        req.setReservationDate(LocalDate.now().plusDays(2));
        req.setReservationTime(LocalTime.of(19, 30));
        req.setSpecialRequests("Window seat please");

        Reservation res = reservationService.createReservation(r1.getId(), req);

        assertNotNull(res.getId());
        assertEquals("Alice Smith", res.getCustomerName());
        assertEquals(r1.getId(), res.getRestaurantId());
        assertEquals(ReservationStatus.PENDING, res.getStatus());
        assertEquals(4, res.getPartySize());
    }

    @Test
    void testCreateReservationRejectsPastDate() {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setCustomerName("Past User");
        req.setMobile("9876543210");
        req.setPartySize(2);
        req.setReservationDate(LocalDate.now().minusDays(1));
        req.setReservationTime(LocalTime.of(12, 0));

        assertThrows(IllegalArgumentException.class, () ->
                reservationService.createReservation(r1.getId(), req));
    }

    @Test
    void testTableCapacityValidation() {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setCustomerName("Big Group");
        req.setMobile("9876543210");
        req.setPartySize(5); // Party of 5
        req.setReservationDate(LocalDate.now().plusDays(3));
        req.setReservationTime(LocalTime.of(20, 0));

        Reservation res = reservationService.createReservation(r1.getId(), req);

        // Assigning to table with capacity 2 must fail
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.assignTable(res.getId(), tableR1Small.getId(), r1.getId()));

        // Assigning to table with capacity 6 must succeed
        Reservation confirmed = reservationService.assignTable(res.getId(), tableR1Large.getId(), r1.getId());
        assertEquals(tableR1Large.getId(), confirmed.getAssignedTableId());
        assertEquals(ReservationStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    void testDoubleBookingPrevention() {
        LocalDate date = LocalDate.now().plusDays(5);

        CreateReservationRequest req1 = new CreateReservationRequest();
        req1.setCustomerName("First Booking");
        req1.setMobile("9876543210");
        req1.setPartySize(2);
        req1.setReservationDate(date);
        req1.setReservationTime(LocalTime.of(19, 0));
        Reservation res1 = reservationService.createReservation(r1.getId(), req1);

        // Assign table to first booking
        reservationService.assignTable(res1.getId(), tableR1Small.getId(), r1.getId());

        // Second booking within 30 minutes of first booking on same day
        CreateReservationRequest req2 = new CreateReservationRequest();
        req2.setCustomerName("Second Booking Overlap");
        req2.setMobile("9876543211");
        req2.setPartySize(2);
        req2.setReservationDate(date);
        req2.setReservationTime(LocalTime.of(19, 30));
        Reservation res2 = reservationService.createReservation(r1.getId(), req2);

        // Assigning same table must be rejected due to overlap
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.assignTable(res2.getId(), tableR1Small.getId(), r1.getId()));
    }

    @Test
    void testMultiRestaurantReservationBoundary() {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setCustomerName("Restaurant 1 User");
        req.setMobile("9876543210");
        req.setPartySize(2);
        req.setReservationDate(LocalDate.now().plusDays(4));
        req.setReservationTime(LocalTime.of(18, 0));
        Reservation res1 = reservationService.createReservation(r1.getId(), req);

        // Trying to access or update R1 reservation from R2 context must throw AccessDeniedException
        assertThrows(AccessDeniedException.class, () ->
                reservationService.getReservationById(res1.getId(), r2.getId()));

        assertThrows(AccessDeniedException.class, () ->
                reservationService.updateStatus(res1.getId(), ReservationStatus.CONFIRMED, r2.getId()));
    }

    @Test
    void testCustomerReserveMvcFlow() throws Exception {
        mockMvc.perform(get("/r/" + r1.getSlug() + "/reserve"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/reserve"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("reservationRequest"));

        mockMvc.perform(post("/r/" + r1.getSlug() + "/reserve")
                        .with(csrf())
                        .param("customerName", "MVC Test Customer")
                        .param("mobile", "9123456789")
                        .param("partySize", "3")
                        .param("reservationDate", LocalDate.now().plusDays(1).toString())
                        .param("reservationTime", "19:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reservations/*"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminReservationsPage() throws Exception {
        mockMvc.perform(get("/admin/reservations").param("restaurantId", String.valueOf(r1.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservations"))
                .andExpect(model().attributeExists("reservations"))
                .andExpect(model().attributeExists("tables"));
    }
}
