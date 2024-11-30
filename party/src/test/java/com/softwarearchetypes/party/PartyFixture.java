package com.softwarearchetypes.party;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

class PartyFixture {

    static PersonTestDataBuilder somePerson() {
        return new PersonTestDataBuilder();
    }

    @SuppressWarnings("unchecked")
    static <T extends Party> PartyAbstractTestDataBuilder<T> somePartyOfType(Class<T> clazz) {
        try {
            Class<?> testDataBuilder = Arrays.stream(PartyAbstractTestDataBuilder.class.getPermittedSubclasses()).filter(implementingClass -> {
                if (implementingClass.getGenericSuperclass() instanceof ParameterizedType parameterizedType) {
                    return clazz.getTypeName().equals(parameterizedType.getActualTypeArguments()[0].getTypeName());
                } else {
                    return false;
                }
            }).findFirst().orElseThrow(() -> new IllegalArgumentException("There is no party of type equal to " + clazz.getTypeName()));
            return (PartyAbstractTestDataBuilder<T>) testDataBuilder.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
