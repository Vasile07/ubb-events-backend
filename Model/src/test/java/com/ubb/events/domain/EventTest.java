package com.ubb.events.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    @Test
    void eventHasExpectedJpaMapping() throws Exception {
        assertTrue(Event.class.isAnnotationPresent(Entity.class));
        Table table = Event.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("events", table.name());
        assertEquals("public", table.schema());

        Field id = Event.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "event_id", Long.class, false);

        Field organization = Event.class.getDeclaredField("organization");
        assertEquals(Organization.class, organization.getType());
        assertTrue(organization.isAnnotationPresent(ManyToOne.class));
        assertFalse(organization.getAnnotation(ManyToOne.class).optional());
        JoinColumn organizationColumn = organization.getAnnotation(JoinColumn.class);
        assertNotNull(organizationColumn);
        assertEquals("organization_id", organizationColumn.name());
        assertFalse(organizationColumn.nullable());

        assertColumn("categoryId", "category_id", Long.class, false);
        assertColumn("title", "title", String.class, false);
        assertColumn("description", "description", String.class, false);
        assertColumn("date", "date", LocalDate.class, false);
        assertColumn("startTime", "start_time", LocalTime.class, false);
        assertColumn("endTime", "end_time", LocalTime.class, true);
        assertColumn("location", "location", String.class, true);
        assertColumn("capacity", "capacity", Long.class, true);

        Field status = Event.class.getDeclaredField("status");
        assertEquals(EventStatus.class, status.getType());
        assertNotNull(status.getAnnotation(Enumerated.class));
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertColumn("status", "status", EventStatus.class, false);

        assertFalse(hasFieldNamed("deleted"));
        assertFalse(hasFieldNamed("creator"));
    }

    @Test
    void statusValuesMatchSpecification() {
        assertArrayEquals(new String[]{"DRAFT", "PUBLISHED", "CANCELED"},
                java.util.Arrays.stream(EventStatus.values()).map(Enum::name).toArray(String[]::new));
    }

    private static void assertColumn(String fieldName, String columnName, Class<?> type, boolean nullable) throws Exception {
        Field field = Event.class.getDeclaredField(fieldName);
        assertEquals(type, field.getType());
        Column column = field.getAnnotation(Column.class);
        assertNotNull(column, fieldName + " must be mapped with @Column");
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());
    }

    private static boolean hasFieldNamed(String name) {
        try {
            Event.class.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }
}
