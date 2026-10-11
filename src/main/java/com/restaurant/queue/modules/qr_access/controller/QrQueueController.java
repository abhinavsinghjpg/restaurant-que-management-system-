package com.restaurant.queue.modules.qr_access.controller;

import com.restaurant.queue.config.AppConfig;
import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.exception.ResourceNotFoundException;
import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import com.restaurant.queue.modules.qr_access.service.QrCodeService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Controller
public class QrQueueController {

    private final RestaurantRepository restaurantRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final CustomerQueueService customerQueueService;
    private final QrCodeService qrCodeService;
    private final AppConfig appConfig;

    public QrQueueController(RestaurantRepository restaurantRepository,
                             QueueEntryRepository queueEntryRepository,
                             CustomerQueueService customerQueueService,
                             QrCodeService qrCodeService,
                             AppConfig appConfig) {
        this.restaurantRepository = restaurantRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.customerQueueService = customerQueueService;
        this.qrCodeService = qrCodeService;
        this.appConfig = appConfig;
    }

    /**
     * Direct QR landing page for a restaurant.
     * Shows live queue summary and form to generate token.
     */
    @GetMapping("/r/{slug}")
    public String showRestaurantQueue(@PathVariable String slug,
                                      HttpServletRequest request,
                                      Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with QR slug: " + slug));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            model.addAttribute("errorTitle", "Restaurant Inactive");
            model.addAttribute("errorMessage", "This restaurant queue is currently closed or inactive.");
            return "error/error";
        }

        long waitingCount = queueEntryRepository.countByRestaurantIdAndStatus(restaurant.getId(), QueueStatus.WAITING);

        List<QueueEntry> calledList = queueEntryRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurant.getId(), QueueStatus.CALLED);
        QueueEntry currentlyCalled = calledList.isEmpty() ? null : calledList.get(0);

        List<QueueEntry> activeQueue = queueEntryRepository.findActiveQueueByRestaurantId(restaurant.getId());

        // Construct QR code target URL
        String qrUrl = ServletUriComponentsBuilder.fromContextPath(request)
                .path("/r/" + restaurant.getSlug())
                .toUriString();

        String qrBase64 = qrCodeService.generateQrCodeBase64(qrUrl, 250, 250);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("waitingCount", waitingCount);
        model.addAttribute("currentlyCalled", currentlyCalled);
        model.addAttribute("estimatedWaitMins", (int) (waitingCount * appConfig.getAvgServiceTimeMinutes()));
        model.addAttribute("activeQueue", activeQueue);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("qrBase64", qrBase64);
        if (!model.containsAttribute("joinRequest")) {
            model.addAttribute("joinRequest", new JoinQueueRequest());
        }

        return "customer/qr_join";
    }

    /**
     * Handles customer queue joining for a specific restaurant via its QR page.
     */
    @PostMapping("/r/{slug}/join")
    public String processRestaurantJoin(@PathVariable String slug,
                                        @Valid @ModelAttribute("joinRequest") JoinQueueRequest joinRequest,
                                        BindingResult bindingResult,
                                        HttpServletRequest request,
                                        Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with QR slug: " + slug));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            model.addAttribute("errorTitle", "Restaurant Inactive");
            model.addAttribute("errorMessage", "This restaurant queue is currently inactive.");
            return "error/error";
        }

        if (bindingResult.hasErrors()) {
            return showRestaurantQueue(slug, request, model);
        }

        QueueEntry entry = customerQueueService.joinQueue(restaurant.getId(), joinRequest);
        return "redirect:/queue/" + entry.getTokenNumber();
    }

    /**
     * Streams PNG QR code image for a restaurant.
     */
    @GetMapping(value = "/r/{slug}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> streamQrCode(@PathVariable String slug, HttpServletRequest request) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with QR slug: " + slug));

        String qrUrl = ServletUriComponentsBuilder.fromContextPath(request)
                .path("/r/" + restaurant.getSlug())
                .toUriString();

        byte[] image = qrCodeService.generateQrCodePng(qrUrl, 350, 350);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + slug + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    /**
     * Print-ready poster view for restaurants.
     */
    @GetMapping("/r/{slug}/print")
    public String printRestaurantQr(@PathVariable String slug, HttpServletRequest request, Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with QR slug: " + slug));

        String qrUrl = ServletUriComponentsBuilder.fromContextPath(request)
                .path("/r/" + restaurant.getSlug())
                .toUriString();

        String qrBase64 = qrCodeService.generateQrCodeBase64(qrUrl, 450, 450);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("qrBase64", qrBase64);
        return "customer/qr_print";
    }

    /**
     * Admin shortcut to print QR code by restaurant ID.
     */
    @GetMapping("/admin/restaurants/{id}/print-qr")
    public String adminPrintQr(@PathVariable Long id, HttpServletRequest request, Model model) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        return printRestaurantQr(restaurant.getSlug(), request, model);
    }
}
