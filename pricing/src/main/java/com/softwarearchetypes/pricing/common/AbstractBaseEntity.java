package com.softwarearchetypes.pricing.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractBaseEntity {

    @Id
    @Column(name = "id")
    @UuidGenerator
    private UUID id;

    @Column(name = "creation_date")
    private OffsetDateTime creationDate;

    protected AbstractBaseEntity(OffsetDateTime creationDate) {
        Objects.requireNonNull(creationDate, "Creation date cannot be null");
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
}
