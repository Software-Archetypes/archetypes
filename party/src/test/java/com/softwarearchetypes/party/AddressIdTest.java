package com.softwarearchetypes.party;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressIdTest {

    @Test
    void twoAddressIdsShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        AddressId firstAddressId = AddressId.of(UUID.randomUUID());
        AddressId secondAddressId = AddressId.of(UUID.randomUUID());

        //expect
        assertNotEquals(firstAddressId, secondAddressId);
    }

    @Test
    void twoAddressIdsShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        UUID value = UUID.randomUUID();

        //expect
        assertEquals(AddressId.of(value), AddressId.of(value));
    }

    @Test
    void AddressIdIsConvertibleToTheValueItWasCreatedFrom() {
        //given
        UUID value = UUID.randomUUID();
        AddressId addressId = AddressId.of(value);

        //expect
        assertEquals(value.toString(), addressId.asString());
    }

    @Test
    void shouldNotAllowToCreateAddressIdForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> AddressId.of(null));
    }
}