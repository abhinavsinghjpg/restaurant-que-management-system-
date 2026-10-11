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

    /** Find a queue entry by restaurant and token. */
    Optional<QueueEntry> findByRestaurantIdAndTokenNumber(Long restaurantId, String tokenNumber);

    /** Find by id and restaurant id for tenant isolation. */
    Optional<QueueEntry> findByIdAndRestaurantId(Long id, Long restaurantId);

    /** Count entries with status WAITING that joined before a given time (for position calculation). */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.status = 'WAITING' AND q.joinTime < :joinTime")
    long countWaitingBefore(@Param("joinTime") LocalDateTime joinTime);

    /** Restaurant-scoped waiting count before a given time. */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.status = 'WAITING' AND q.joinTime < :joinTime")
    long countWaitingBeforeByRestaurant(@Param("restaurantId") Long restaurantId, @Param("joinTime") LocalDateTime joinTime);

    /** Count how many entries were created today (for token generation). */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.joinTime >= :startOfDay")
    long countTodaysEntries(@Param("startOfDay") LocalDateTime startOfDay);

    /** Restaurant-scoped today's entry count (for token generation). */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.joinTime >= :startOfDay")
    long countTodaysEntriesByRestaurant(@Param("restaurantId") Long restaurantId, @Param("startOfDay") LocalDateTime startOfDay);

    /** Get all WAITING entries ordered by join time (for call-next). */
    List<QueueEntry> findByStatusOrderByJoinTimeAsc(QueueStatus status);

    /** Restaurant-scoped WAITING entries ordered by join time. */
    List<QueueEntry> findByRestaurantIdAndStatusOrderByJoinTimeAsc(Long restaurantId, QueueStatus status);

    /** Get all active entries (WAITING + CALLED) ordered by join time for the staff queue view. */
    @Query("SELECT q FROM QueueEntry q WHERE q.status IN ('WAITING', 'CALLED') ORDER BY q.joinTime ASC")
    List<QueueEntry> findActiveQueue();

    /** Restaurant-scoped active entries. */
    @Query("SELECT q FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.status IN ('WAITING', 'CALLED') ORDER BY q.joinTime ASC")
    List<QueueEntry> findActiveQueueByRestaurantId(@Param("restaurantId") Long restaurantId);

    /** Count entries by status. */
    long countByStatus(QueueStatus status);

    /** Restaurant-scoped count by status. */
    long countByRestaurantIdAndStatus(Long restaurantId, QueueStatus status);

    /** Count entries by status where join_time is on a specific date. */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.status = :status AND q.joinTime >= :startOfDay AND q.joinTime < :endOfDay")
    long countByStatusAndDate(@Param("status") QueueStatus status,
                               @Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);

    /** Restaurant-scoped count by status and date. */
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.status = :status AND q.joinTime >= :startOfDay AND q.joinTime < :endOfDay")
    long countByRestaurantIdAndStatusAndDate(@Param("restaurantId") Long restaurantId,
                                              @Param("status") QueueStatus status,
                                              @Param("startOfDay") LocalDateTime startOfDay,
                                              @Param("endOfDay") LocalDateTime endOfDay);

    /** Get SEATED entries from today for avg wait time calculation. */
    @Query("SELECT q FROM QueueEntry q WHERE q.status = 'SEATED' AND q.seatedTime >= :startOfDay")
    List<QueueEntry> findSeatedTodayFrom(@Param("startOfDay") LocalDateTime startOfDay);

    /** Restaurant-scoped seated entries from today. */
    @Query("SELECT q FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.status = 'SEATED' AND q.seatedTime >= :startOfDay")
    List<QueueEntry> findSeatedTodayFromByRestaurant(@Param("restaurantId") Long restaurantId, @Param("startOfDay") LocalDateTime startOfDay);

    List<QueueEntry> findByRestaurantIdAndJoinTimeBetweenOrderByJoinTimeAsc(Long restaurantId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT q FROM QueueEntry q WHERE q.restaurantId = :restaurantId AND q.status = 'SEATED' AND q.seatedTime >= :start AND q.seatedTime <= :end")
    List<QueueEntry> findSeatedBetweenByRestaurant(@Param("restaurantId") Long restaurantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
