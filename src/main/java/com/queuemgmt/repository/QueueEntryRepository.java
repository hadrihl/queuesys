package com.queuemgmt.repository;

import com.queuemgmt.model.QueueEntry;
import com.queuemgmt.model.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    Optional<QueueEntry> findByTrackingNumber(String trackingNumber);

    Optional<QueueEntry> findByAccessToken(String accessToken);

    List<QueueEntry> findByStatusOrderByQueueNumberAsc(QueueStatus status);

    List<QueueEntry> findByStatusInOrderByQueueNumberAsc(List<QueueStatus> statuses);

    @Query("SELECT COALESCE(MAX(q.queueNumber), 0) FROM QueueEntry q")
    Integer findMaxQueueNumber();

    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.status IN ('WAITING', 'CALLED', 'IN_SERVICE') AND q.queueNumber < :queueNumber")
    Integer countQueueAhead(Integer queueNumber);
}
