package com.ubb.events.persistence;

import com.ubb.events.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByEventIdOrderByIdAsc(Long eventId);
}
