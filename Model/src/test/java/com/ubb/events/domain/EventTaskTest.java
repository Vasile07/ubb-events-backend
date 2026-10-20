package com.ubb.events.domain;

import jakarta.persistence.Column;
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

class EventTaskTest {
    @Test
    void eventTaskHasExpectedJpaMapping() throws Exception {
        assertTrue(EventTask.class.isAnnotationPresent(Entity.class));
        Table table = EventTask.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("tasks", table.name());
        assertEquals("public", table.schema());

        Field id = EventTask.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertNotNull(id.getAnnotation(Column.class));
        assertEquals("task_id", id.getAnnotation(Column.class).name());
        assertFalse(id.getAnnotation(Column.class).nullable());

        Field description = EventTask.class.getDeclaredField("description");
        assertEquals(String.class, description.getType());
        assertNotNull(description.getAnnotation(Column.class));
        assertEquals("description", description.getAnnotation(Column.class).name());
        assertFalse(description.getAnnotation(Column.class).nullable());

        Field assignedUser = EventTask.class.getDeclaredField("assignedUser");
        assertEquals(User.class, assignedUser.getType());
        assertTrue(assignedUser.isAnnotationPresent(ManyToOne.class));
        assertTrue(assignedUser.getAnnotation(ManyToOne.class).optional());
        assertNotNull(assignedUser.getAnnotation(JoinColumn.class));
        assertEquals("assigned_user_id", assignedUser.getAnnotation(JoinColumn.class).name());
        assertTrue(assignedUser.getAnnotation(JoinColumn.class).nullable());

        Field event = EventTask.class.getDeclaredField("event");
        assertEquals(Event.class, event.getType());
        assertTrue(event.isAnnotationPresent(ManyToOne.class));
        assertFalse(event.getAnnotation(ManyToOne.class).optional());
        assertNotNull(event.getAnnotation(JoinColumn.class));
        assertEquals("event_id", event.getAnnotation(JoinColumn.class).name());
        assertFalse(event.getAnnotation(JoinColumn.class).nullable());

        assertThrows(NoSuchFieldException.class, () -> EventTask.class.getDeclaredField("title"));
        assertThrows(NoSuchFieldException.class, () -> EventTask.class.getDeclaredField("dueDate"));
        assertThrows(NoSuchFieldException.class, () -> EventTask.class.getDeclaredField("status"));
    }
}
