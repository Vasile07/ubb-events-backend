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

import static org.junit.jupiter.api.Assertions.*;

class MembershipTest {
    @Test
    void membershipHasExpectedJpaMapping() throws Exception {
        assertTrue(Membership.class.isAnnotationPresent(Entity.class));
        Table table = Membership.class.getAnnotation(Table.class);
        assertEquals("memberships", table.name());
        assertEquals("public", table.schema());

        Field id = Membership.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());

        assertAssociation("user", "user_id");
        assertAssociation("organization", "organization_id");
        assertEnumColumn("role", "role", MembershipRole.class);
        assertEnumColumn("status", "status", MembershipStatus.class);
    }

    @Test
    void enumValuesMatchSpecification() {
        assertArrayEquals(new MembershipRole[]{MembershipRole.MEMBER, MembershipRole.MANAGER}, MembershipRole.values());
        assertArrayEquals(new MembershipStatus[]{MembershipStatus.PENDING, MembershipStatus.ACTIVE}, MembershipStatus.values());
    }

    private static void assertAssociation(String fieldName, String columnName) throws Exception {
        Field field = Membership.class.getDeclaredField(fieldName);
        assertTrue(field.isAnnotationPresent(ManyToOne.class));
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals(columnName, joinColumn.name());
        assertFalse(joinColumn.nullable());
    }

    private static void assertEnumColumn(String fieldName, String columnName, Class<?> enumType) throws Exception {
        Field field = Membership.class.getDeclaredField(fieldName);
        assertEquals(enumType, field.getType());
        assertEquals(EnumType.STRING, field.getAnnotation(Enumerated.class).value());
        Column column = field.getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.name());
        assertTrue(column.nullable());
    }
}
