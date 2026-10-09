package com.restaurant.queue.modules.queue_management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records every important action taken on a queue entry.
 * Used for history, auditing, and statistics.
 */
@Entity
@Table(name = "queue_history")
public class QueueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_entry_id", nullable = false)
    private Long queueEntryId;

    @Column(name = "action", nullable = false)
    private String action;  // JOINED, CALLED, SEATED, SKIPPED, CANCELLED

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Long getId() { return id; }

    public Long getQueueEntryId() { return queueEntryId; }
    public void setQueueEntryId(Long queueEntryId) { this.queueEntryId = queueEntryId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
