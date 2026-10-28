package com.ubb.events.persistence;

import com.ubb.events.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    long countByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    java.util.Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);
    @Query("select r from Registration r join fetch r.event e where r.user.id = :userId order by e.date asc, e.startTime asc, r.id asc")
    List<Registration> findAllByUserId(@Param("userId") Long userId);
}
