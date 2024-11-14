package com.softwarearchetypes.party;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.RoleAdded;
import com.softwarearchetypes.events.RoleAdditionFailed;
import com.softwarearchetypes.events.RoleAdditionSkipped;
import com.softwarearchetypes.events.RoleAdditionSucceeded;

import static com.softwarearchetypes.common.CollectionFixture.copyAndAdd;
import static com.softwarearchetypes.common.RandomFixture.randomElementOf;
import static com.softwarearchetypes.party.RoleFixture.someRole;
import static com.softwarearchetypes.party.RoleFixture.someRoleSetOfSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RolesTest {

    @Test
    void emptyRoleAggregatorIsCreatedForNullRoleSet() {
        //given
        Roles roles = Roles.from(null);

        //expect
        assertEquals(roles.asSet(), Set.of());
    }

    @Test
    void emptyRoleAggregatorIsCreatedForEmptyRoleSet() {
        //given
        Roles roles = Roles.from(Set.of());

        //expect
        assertEquals(roles.asSet(), Set.of());
    }

    @Test
    void roleAggregatorIsConvertibleToTheRoleSetItWasCreatedFrom() {
        //given
        Set<Role> roleSet = someRoleSetOfSize(5);
        Roles roles = Roles.from(roleSet);

        //expect
        assertEquals(roles.asSet(), roleSet);
    }

    @Test
    void twoRoleAggregatorsShouldNotBeEqualWhenCreatedForDifferentRoleSets() {
        //given
        Roles firstRoles = Roles.from(someRoleSetOfSize(5));
        Roles secondRoles = Roles.from(someRoleSetOfSize(5));

        //expect
        assertNotEquals(firstRoles, secondRoles);
    }

    @Test
    void twoRoleAggregatorsShouldBeEqualWhenCreatedForTheSameRoleSet() {
        //given
        Set<Role> roleSet = someRoleSetOfSize(5);

        //expect
        assertEquals(Roles.from(roleSet), Roles.from(roleSet));
    }

    @Test
    void addingExistingRoleShouldBeSkippedWhenRoleAlreadyExistsInRoleAggregator() {
        //given
        Set<Role> initialRoleSet = someRoleSetOfSize(5);
        Roles roles = Roles.from(initialRoleSet);
        Role duplicateRoleToBeAdded = randomElementOf(initialRoleSet);

        //when
        Result<RoleAdditionFailed, RoleAdditionSucceeded> result = roles.add(duplicateRoleToBeAdded);

        //then
        assertEquals(RoleAdditionSkipped.dueToDuplicationFor(duplicateRoleToBeAdded.asString()), result.getSuccess());
        assertEquals(initialRoleSet, roles.asSet());
    }

    @Test
    void newRoleShouldBeAddedWithoutModifyingExistingRoleAggregator() {
        //given
        Set<Role> initialRoleSet = someRoleSetOfSize(5);
        Roles roles = Roles.from(initialRoleSet);

        //and
        Role roleToBeAdded = someRole();
        Set<Role> expectedRoleSet = copyAndAdd(initialRoleSet, roleToBeAdded);

        //when
        Result<RoleAdditionFailed, RoleAdditionSucceeded> result = roles.add(roleToBeAdded);

        //then
        assertEquals(new RoleAdded(roleToBeAdded.asString()), result.getSuccess());
        assertEquals(expectedRoleSet, roles.asSet());
    }
}