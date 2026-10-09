package com.restaurant.queue.modules.queue_management.repository;

import com.restaurant.queue.modules.queue_management.model.QueueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueHistoryRepository extends JpaRepository<QueueHistory, Long> {
    List<QueueHistory> findByQueueEntryIdOrderByTimestampDesc(Long queueEntryId);
}
