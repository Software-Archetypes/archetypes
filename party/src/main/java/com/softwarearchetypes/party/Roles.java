package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.RoleAdded;
import com.softwarearchetypes.events.RoleAdditionFailed;
import com.softwarearchetypes.events.RoleAdditionSkipped;
import com.softwarearchetypes.events.RoleAdditionSucceeded;
import com.softwarearchetypes.events.RoleRemovalFailed;
import com.softwarearchetypes.events.RoleRemovalSkipped;
import com.softwarearchetypes.events.RoleRemovalSucceeded;
import com.softwarearchetypes.events.RoleRemoved;

import static com.softwarearchetypes.common.Preconditions.checkNotNull;

public final class Roles {

    private final Set<Role> values;

    private Roles(Set<Role> values) {
        this.values = Optional.ofNullable(values).map(HashSet::new).orElse(new HashSet<>());
    }

    static Roles from(Set<Role> values) {
        return new Roles(values);
    }

    Result<RoleAdditionFailed, RoleAdditionSucceeded> add(Role role) {
        checkNotNull(role, "Role cannot be null");
        if (!values.contains(role)) {
            values.add(role);
            return Result.success(new RoleAdded(role.asString()));
        } else {
            //for idempotency
            return Result.success(RoleAdditionSkipped.dueToDuplicationFor(role.asString()));
        }
    }

    Set<Role> asSet() {
        return Set.copyOf(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Roles roles)) {
            return false;
        }
        return Objects.equals(values, roles.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    Result<RoleRemovalFailed, RoleRemovalSucceeded> remove(Role role) {
        checkNotNull(role, "Role cannot be null");
        if (values.contains(role)) {
            values.remove(role);
            return Result.success(new RoleRemoved(role.asString()));
        } else {
            //for idempotency
            return Result.success(RoleRemovalSkipped.dueToMissingRoleFor(role.asString()));
        }
    }
}
