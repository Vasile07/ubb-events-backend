package com.ubb.events.persistence;

import com.ubb.events.domain.EventTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTaskRepository extends JpaRepository<EventTask, Long> {
    List<EventTask> findAllByEventIdOrderByIdAsc(Long eventId);
    List<EventTask> findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(Long userId);
}
