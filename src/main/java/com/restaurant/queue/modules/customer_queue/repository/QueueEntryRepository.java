package com.restaurant.queue.modules.customer_queue.repository;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    /** Find a queue entry by its token. */
    Optional<QueueEntry> findByTokenNumber(String tokenNumber);

    /** Count entries with status WAITING that joined before a given time (for position calculation). */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.status = 'WAITING' AND q.joinTime < :joinTime")
    long countWaitingBefore(@Param("joinTime") LocalDateTime joinTime);

    /** Count how many entries were created today (for token generation). */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.joinTime >= :startOfDay")
    long countTodaysEntries(@Param("startOfDay") LocalDateTime startOfDay);

    /** Get all WAITING entries ordered by join time (for call-next). */
    List<QueueEntry> findByStatusOrderByJoinTimeAsc(QueueStatus status);

    /** Get all active entries (WAITING + CALLED) ordered by join time for the staff queue view. */
    @Query("SELECT q FROM QueueEntry q WHERE q.status IN ('WAITING', 'CALLED') ORDER BY q.joinTime ASC")
    List<QueueEntry> findActiveQueue();

    /** Count entries by status. */
    long countByStatus(QueueStatus status);

    /** Count entries by status where join_time is on a specific date. */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.status = :status AND q.joinTime >= :startOfDay AND q.joinTime < :endOfDay")
    long countByStatusAndDate(@Param("status") QueueStatus status,
                               @Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);

    /** Get SEATED entries from today for avg wait time calculation. */
    @Query("SELECT q FROM QueueEntry q WHERE q.status = 'SEATED' AND q.seatedTime >= :startOfDay")
    List<QueueEntry> findSeatedTodayFrom(@Param("startOfDay") LocalDateTime startOfDay);
}
