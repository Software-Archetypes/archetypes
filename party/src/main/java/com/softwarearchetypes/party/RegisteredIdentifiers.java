package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.RegisteredIdentifierAdded;
import com.softwarearchetypes.events.RegisteredIdentifierAdditionSkipped;
import com.softwarearchetypes.events.RegisteredIdentifierAdditionSucceeded;
import com.softwarearchetypes.events.RegisteredIdentifierRemovalSkipped;
import com.softwarearchetypes.events.RegisteredIdentifierRemovalSucceeded;
import com.softwarearchetypes.events.RegisteredIdentifierRemoved;
import com.softwarearchetypes.events.RegisteredIdentityAdditionFailed;
import com.softwarearchetypes.events.RegisteredIdentityRemovalFailed;

import static com.softwarearchetypes.common.Preconditions.checkNotNull;

public final class RegisteredIdentifiers {

    private final Set<RegisteredIdentifier> values;

    private RegisteredIdentifiers(Set<RegisteredIdentifier> values) {
        this.values = Optional.ofNullable(values).map(HashSet::new).orElse(new HashSet<>());
    }

    static RegisteredIdentifiers from(Set<RegisteredIdentifier> values) {
        return new RegisteredIdentifiers(values);
    }

    Result<RegisteredIdentityAdditionFailed, RegisteredIdentifierAdditionSucceeded> add(RegisteredIdentifier identifier) {
        checkNotNull(identifier, "Registered identifier cannot be null");
        if (!values.contains(identifier)) {
            values.add(identifier);
            return Result.success(new RegisteredIdentifierAdded(identifier.type(), identifier.asString()));
        } else {
            //for idempotency
            return Result.success(RegisteredIdentifierAdditionSkipped.dueToDataDuplicationFor(identifier.type(), identifier.asString()));
        }
    }

    Result<RegisteredIdentityRemovalFailed, RegisteredIdentifierRemovalSucceeded> remove(RegisteredIdentifier identifier) {
        checkNotNull(identifier, "Registered identifier cannot be null");
        if (values.contains(identifier)) {
            values.remove(identifier);
            return Result.success(new RegisteredIdentifierRemoved(identifier.type(), identifier.asString()));
        } else {
            //for idempotency
            return Result.success(RegisteredIdentifierRemovalSkipped.dueToMissingIdentifierFor(identifier.type(), identifier.asString()));
        }
    }

    Set<RegisteredIdentifier> asSet() {
        return Set.copyOf(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegisteredIdentifiers that)) {
            return false;
        }
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
