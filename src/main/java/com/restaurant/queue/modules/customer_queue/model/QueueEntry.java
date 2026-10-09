package com.restaurant.queue.modules.customer_queue.model;

import com.restaurant.queue.common.constants.QueueStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a customer's queue entry.
 * Each entry is created when a customer joins the queue.
 */
@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, unique = true)
    private String tokenNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "mobile", nullable = false)
    private String mobile;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QueueStatus status;

    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;

    @Column(name = "called_time")
    private LocalDateTime calledTime;

    @Column(name = "seated_time")
    private LocalDateTime seatedTime;

    // ============================================================
    // Constructors
    // ============================================================

    public QueueEntry() {}

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Long getId() { return id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }

    public LocalDateTime getSeatedTime() { return seatedTime; }
    public void setSeatedTime(LocalDateTime seatedTime) { this.seatedTime = seatedTime; }
}
