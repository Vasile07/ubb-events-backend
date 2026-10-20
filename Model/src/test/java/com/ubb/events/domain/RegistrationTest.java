package com.ubb.events.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {
    @Test
    void registrationHasExpectedJpaMapping() throws Exception {
        assertTrue(Registration.class.isAnnotationPresent(Entity.class));
        Table table = Registration.class.getAnnotation(Table.class);
        assertEquals("registrations", table.name());
        assertEquals("public", table.schema());
        assertEquals(0, table.uniqueConstraints().length);

        Field id = Registration.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertFalse(hasFieldNamed("status"));
        assertFalse(hasFieldNamed("eventId"));

        Field event = Registration.class.getDeclaredField("event");
        assertEquals(Event.class, event.getType());
        assertTrue(event.isAnnotationPresent(ManyToOne.class));
        assertFalse(event.getAnnotation(ManyToOne.class).optional());
        JoinColumn eventColumn = event.getAnnotation(JoinColumn.class);
        assertNotNull(eventColumn);
        assertEquals("event_id", eventColumn.name());
        assertFalse(eventColumn.nullable());

        Field user = Registration.class.getDeclaredField("user");
        assertTrue(user.isAnnotationPresent(ManyToOne.class));
        JoinColumn userColumn = user.getAnnotation(JoinColumn.class);
        assertNotNull(userColumn);
        assertEquals("user_id", userColumn.name());
        assertFalse(userColumn.nullable());
    }

    private static boolean hasFieldNamed(String name) {
        try {
            Registration.class.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }
}
