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

class FollowUpTest {
    @Test
    void followUpHasExpectedJpaMapping() throws Exception {
        assertTrue(FollowUp.class.isAnnotationPresent(Entity.class));
        Table table = FollowUp.class.getAnnotation(Table.class);
        assertEquals("followups", table.name());
        assertEquals("public", table.schema());

        Field id = FollowUp.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());

        Field eventId = FollowUp.class.getDeclaredField("eventId");
        Column eventColumn = eventId.getAnnotation(Column.class);
        assertNotNull(eventColumn);
        assertEquals("event_id", eventColumn.name());
        assertFalse(eventColumn.nullable());

        Field content = FollowUp.class.getDeclaredField("content");
        Column contentColumn = content.getAnnotation(Column.class);
        assertNotNull(contentColumn);
        assertEquals("content", contentColumn.name());
        assertFalse(contentColumn.nullable());

        assertFalse(hasFieldNamed("summary"));
        assertFalse(hasFieldNamed("feedback"));
    }

    private static boolean hasFieldNamed(String name) {
        try {
            FollowUp.class.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }
}
