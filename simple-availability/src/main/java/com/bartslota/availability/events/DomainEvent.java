package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssetActivated.class, name = AssetActivated.TYPE),
        @JsonSubTypes.Type(value = AssetActivationRejected.class, name = AssetActivationRejected.TYPE),
        @JsonSubTypes.Type(value = AssetLocked.class, name = AssetLocked.TYPE),
        @JsonSubTypes.Type(value = AssetLockExpired.class, name = AssetLockExpired.TYPE),
        @JsonSubTypes.Type(value = AssetLockRejected.class, name = AssetLockRejected.TYPE),
        @JsonSubTypes.Type(value = AssetRegistered.class, name = AssetRegistered.TYPE),
        @JsonSubTypes.Type(value = AssetRegistrationRejected.class, name = AssetRegistrationRejected.TYPE),
        @JsonSubTypes.Type(value = AssetUnlocked.class, name = AssetUnlocked.TYPE),
        @JsonSubTypes.Type(value = AssetUnlockingRejected.class, name = AssetUnlockingRejected.TYPE),
        @JsonSubTypes.Type(value = AssetWithdrawalRejected.class, name = AssetWithdrawalRejected.TYPE),
        @JsonSubTypes.Type(value = AssetWithdrawn.class, name = AssetWithdrawn.TYPE)
})
public interface DomainEvent {

    UUID getId();

    Instant getOccurredAt();

    String getType();
}
