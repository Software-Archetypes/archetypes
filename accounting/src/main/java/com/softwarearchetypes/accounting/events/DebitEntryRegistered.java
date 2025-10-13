package com.softwarearchetypes.accounting.events;

import java.time.Instant;
import java.util.UUID;

import com.softwarearchetypes.quantity.money.Money;

public record DebitEntryRegistered(UUID id, Instant occurredAt, Instant appliesAt, UUID entryId, UUID accountId, UUID transactionId, Money amount) implements AccountingEvent {

    static final String TYPE = "DebitEntryRegistered";

    public String type() {
        return TYPE;
    }

}
