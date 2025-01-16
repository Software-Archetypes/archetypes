package com.softwarearchetypes.availability.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Register.class, name = Register.TYPE),
        @JsonSubTypes.Type(value = Activate.class, name = Activate.TYPE),
        @JsonSubTypes.Type(value = Withdraw.class, name = Withdraw.TYPE),
        @JsonSubTypes.Type(value = Lock.class, name = Lock.TYPE),
        @JsonSubTypes.Type(value = LockIndefinitely.class, name = LockIndefinitely.TYPE),
        @JsonSubTypes.Type(value = Unlock.class, name = Unlock.TYPE)
})
public sealed interface Command permits Register, Activate, Withdraw, Lock, LockIndefinitely, Unlock {

    String getType();

}
