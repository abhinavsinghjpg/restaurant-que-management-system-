package com.restaurant.queue.modules.customer_queue.controller;

import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.dto.QueueStatusResponse;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling all customer-facing queue pages.
 * No authentication required for any of these routes.
 */
@Controller
public class CustomerQueueController {

    private final CustomerQueueService customerQueueService;

    public CustomerQueueController(CustomerQueueService customerQueueService) {
        this.customerQueueService = customerQueueService;
    }

    /** Home / landing page. */
    @GetMapping("/")
    public String home() {
        return "customer/home";
    }

    /** Display the join queue form. */
    @GetMapping("/queue/join")
    public String showJoinForm(Model model) {
        model.addAttribute("joinRequest", new JoinQueueRequest());
        return "customer/join";
    }

    /** Process the join queue form submission. */
    @PostMapping("/queue/join")
    public String processJoin(@Valid @ModelAttribute("joinRequest") JoinQueueRequest request,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "customer/join";
        }

        QueueEntry entry = customerQueueService.joinQueue(request);
        return "redirect:/queue/" + entry.getTokenNumber();
    }

    /** Show queue status page for a given token. */
    @GetMapping("/queue/{token}")
    public String showQueueStatus(@PathVariable String token, Model model) {
        QueueStatusResponse status = customerQueueService.getQueueStatus(token);
        model.addAttribute("queueStatus", status);
        return "customer/status";
    }

    /** Cancel a queue entry (POST to avoid accidental GET cancellations). */
    @PostMapping("/queue/{token}/cancel")
    public String cancelQueue(@PathVariable String token, RedirectAttributes redirectAttributes) {
        customerQueueService.cancelQueue(token);
        redirectAttributes.addFlashAttribute("successMessage", "Your queue entry has been cancelled.");
        return "redirect:/queue/" + token;
    }
}
