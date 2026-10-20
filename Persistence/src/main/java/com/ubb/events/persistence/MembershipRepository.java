package com.ubb.events.persistence;

import com.ubb.events.domain.Membership;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    List<Membership> findAllByUserId(Long userId);
}
