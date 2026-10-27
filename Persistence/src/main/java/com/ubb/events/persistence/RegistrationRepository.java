package com.ubb.events.persistence;

import com.ubb.events.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    long countByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    java.util.Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);
}
