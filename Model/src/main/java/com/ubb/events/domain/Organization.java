package com.ubb.events.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    public Organization(Long id, String name, String description, String logoUrl) {
        this(id, name, description, null, logoUrl);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "logo_url")
    private String logoUrl;
}
