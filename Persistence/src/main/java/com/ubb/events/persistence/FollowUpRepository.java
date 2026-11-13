package com.ubb.events.persistence;

import com.ubb.events.domain.FollowUp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    Optional<FollowUp> findByEvent_IdAndEvent_Organization_Id(Long eventId, Long organizationId);
}
