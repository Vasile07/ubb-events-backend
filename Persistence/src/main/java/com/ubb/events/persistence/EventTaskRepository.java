package com.ubb.events.persistence;

import com.ubb.events.domain.EventTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTaskRepository extends JpaRepository<EventTask, Long> { }
