package com.ubb.events.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationTest {
    @Test
    void organizationHasExpectedJpaMappingAndScalarFields() throws Exception {
        assertTrue(Organization.class.isAnnotationPresent(Entity.class));
        assertEquals("organizations", Organization.class.getAnnotation(Table.class).name());
        assertEquals("public", Organization.class.getAnnotation(Table.class).schema());

        Field id = Organization.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("name", "name", false);
        assertColumn("description", "description", true);
        assertColumn("logoUrl", "logo_url", true);
    }

    @Test
    void organizationStoresAllScalarValues() {
        Organization organization = new Organization(null, "UBB CS", null, null);
        assertEquals("UBB CS", organization.getName());
        assertNull(organization.getDescription());
        assertNull(organization.getLogoUrl());
    }

    private static void assertColumn(String fieldName, String columnName, boolean nullable)
            throws NoSuchFieldException {
        Column column = Organization.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column, fieldName + " must declare @Column");
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());
    }
}
