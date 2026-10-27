package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByOrganizationIdOrderByDateAscStartTimeAsc(Long organizationId);
    Optional<Event> findByIdAndOrganizationId(Long id, Long organizationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Event> findWithLockById(Long id);
}
