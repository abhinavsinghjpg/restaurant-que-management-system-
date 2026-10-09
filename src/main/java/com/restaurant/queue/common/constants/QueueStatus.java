package com.restaurant.queue.common.constants;

/**
 * Enum representing all possible states of a customer's queue entry.
 *
 * State machine:
 *   WAITING -> CALLED -> SEATED
 *   WAITING -> SKIPPED
 *   WAITING -> CANCELLED
 *   CALLED  -> SKIPPED
 *   CALLED  -> SEATED
 */
public enum QueueStatus {
    WAITING,
    CALLED,
    SEATED,
    SKIPPED,
    CANCELLED
}
