package com.ubb.events.persistence;

import com.ubb.events.domain.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationRepositoryTest {
    @Autowired
    private OrganizationRepository repository;

    @Test
    void savesOrganizationWithGeneratedIdAndNullableFields() {
        Organization saved = repository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        assertNotNull(saved.getId());
        Organization loaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("UBB CS", loaded.getName());
        assertNull(loaded.getDescription());
        assertNull(loaded.getLogoUrl());
    }

    @Test
    void rejectsNullName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Organization(null, null, null, null)));
    }
}
