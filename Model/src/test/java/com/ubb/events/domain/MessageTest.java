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

class MessageTest {
    @Test
    void messageHasExpectedJpaMapping() throws Exception {
        assertTrue(Message.class.isAnnotationPresent(Entity.class));
        Table table = Message.class.getAnnotation(Table.class);
        assertEquals("messages", table.name());
        assertEquals("public", table.schema());

        Field id = Message.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertEquals("message_id", id.getAnnotation(Column.class).name());
        assertFalse(id.getAnnotation(Column.class).nullable());

        Field eventId = Message.class.getDeclaredField("eventId");
        assertEquals("event_id", eventId.getAnnotation(Column.class).name());
        assertFalse(eventId.getAnnotation(Column.class).nullable());

        Field sender = Message.class.getDeclaredField("sender");
        assertTrue(sender.isAnnotationPresent(ManyToOne.class));
        assertFalse(sender.getAnnotation(ManyToOne.class).optional());
        assertEquals("sender_id", sender.getAnnotation(JoinColumn.class).name());
        assertFalse(sender.getAnnotation(JoinColumn.class).nullable());

        Field content = Message.class.getDeclaredField("content");
        assertEquals("content", content.getAnnotation(Column.class).name());
        assertFalse(content.getAnnotation(Column.class).nullable());
        assertThrows(NoSuchFieldException.class, () -> Message.class.getDeclaredField("timestamp"));
    }
}
