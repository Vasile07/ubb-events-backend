package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrganizationIdOrderByDateAscStartTimeAsc(Long organizationId);
    Optional<Event> findByIdAndOrganizationId(Long id, Long organizationId);
}
