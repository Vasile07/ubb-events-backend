package com.ubb.events.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void userHasExpectedJpaMappingAndScalarFields() throws Exception {
        assertTrue(User.class.isAnnotationPresent(Entity.class));
        assertEquals("users", User.class.getAnnotation(Table.class).name());
        assertEquals("public", User.class.getAnnotation(Table.class).schema());
        Field id = User.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("firstName", "first_name", false, false);
        assertColumn("lastName", "last_name", false, false);
        assertColumn("email", "email", false, true);
        assertColumn("password", "password", false, true);
    }

    private static void assertColumn(String fieldName, String columnName, boolean nullable, boolean unique)
            throws NoSuchFieldException {
        Column column = User.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column, fieldName + " must declare @Column");
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(unique, column.unique());
    }

    @Test
    void userStoresAllScalarValues() {
        User user = new User(null, "Ada", "Lovelace", "ada@example.com", "hashed-password");
        assertEquals("Ada", user.getFirstName());
        assertEquals("Lovelace", user.getLastName());
        assertEquals("ada@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPassword());
    }
}
