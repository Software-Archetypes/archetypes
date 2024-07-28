package com.softwarearchetypes.pricing.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
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

    public AbstractBaseEntity(Clock clock) {
        Objects.requireNonNull(clock, "Entity needs clock to set creation date");
        this.creationDate = OffsetDateTime.now(clock);
    }

    protected AbstractBaseEntity() {
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
}
