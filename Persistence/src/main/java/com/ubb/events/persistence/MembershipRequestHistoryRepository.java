package com.ubb.events.persistence;

import com.ubb.events.domain.MembershipRequestHistory;
import com.ubb.events.domain.MembershipRequestDetailsStatus;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRequestHistoryRepository extends JpaRepository<MembershipRequestHistory, Long> {
    Optional<MembershipRequestHistory> findByMembershipId(Long membershipId);
    List<MembershipRequestHistory> findAllByOrganizationIdAndStatus(Long organizationId, MembershipRequestDetailsStatus status);
}
