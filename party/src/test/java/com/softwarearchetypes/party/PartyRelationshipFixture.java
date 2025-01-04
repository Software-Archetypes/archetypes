package com.softwarearchetypes.party;

import java.util.function.Supplier;

class PartyRelationshipFixture {

    public static class FixablePartyRelationshipIdSupplier implements Supplier<PartyRelationshipId> {

        private PartyRelationshipId fixedValue;

        public void clear() {
            fixedValue = null;
        }

        public void fixPartyRelationshipIdTo(PartyRelationshipId value) {
            fixedValue = value;
        }

        @Override
        public PartyRelationshipId get() {
            return fixedValue != null ? fixedValue : PartyRelationshipId.random();
        }
    }
}
