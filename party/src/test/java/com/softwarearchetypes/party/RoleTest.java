package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static com.softwarearchetypes.party.RoleFixture.someRoleName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleTest {

    @Test
    void twoRolesShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        Role firstRole = Role.of(someRoleName());
        Role secondRole = Role.of(someRoleName());

        //expect
        assertNotEquals(firstRole, secondRole);
    }

    @Test
    void twoRolesShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        String value = someRoleName();

        //expect
        assertEquals(Role.of(value), Role.of(value));
    }

    @Test
    void roleIsConvertibleToTextualValueItWasCreatedFrom() {
        //given
        String value = someRoleName();
        Role role = Role.of(value);

        //expect
        assertEquals(value, role.asString());
    }

    @Test
    void shouldNotAllowToCreateRoleForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> Role.of(null));
    }

    @Test
    void shouldNotAllowToCreateRoleForEmptyValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> Role.of(""));
    }
}