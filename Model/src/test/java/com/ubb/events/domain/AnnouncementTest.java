package com.ubb.events.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementTest {
    @Test
    void mapsAnnouncementColumnsAndRequiredRelationships() throws Exception {
        assertTrue(Announcement.class.isAnnotationPresent(Entity.class));
        Table table = Announcement.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("announcements", table.name());
        assertEquals("public", table.schema());
        Field id = Announcement.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());
        Column idColumn = id.getAnnotation(Column.class);
        assertNotNull(idColumn);
        assertEquals("announcement_id", idColumn.name());
        assertFalse(idColumn.nullable());

        Field event = Announcement.class.getDeclaredField("event");
        assertTrue(event.isAnnotationPresent(ManyToOne.class));
        assertFalse(event.getAnnotation(ManyToOne.class).optional());
        assertEquals("event_id", event.getAnnotation(JoinColumn.class).name());
        assertFalse(event.getAnnotation(JoinColumn.class).nullable());
        assertEquals("title", Announcement.class.getDeclaredField("title").getAnnotation(Column.class).name());
        assertFalse(Announcement.class.getDeclaredField("title").getAnnotation(Column.class).nullable());
        assertEquals("content", Announcement.class.getDeclaredField("content").getAnnotation(Column.class).name());
        assertFalse(Announcement.class.getDeclaredField("content").getAnnotation(Column.class).nullable());
        assertThrows(NoSuchFieldException.class, () -> Announcement.class.getDeclaredField("author"));
        assertThrows(NoSuchFieldException.class, () -> Announcement.class.getDeclaredField("dateTime"));
    }
}
