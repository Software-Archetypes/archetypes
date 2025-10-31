package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.UUID;

/**
 * Command for reversing an existing transaction.
 * Creates a reversal transaction that inverts all entries of the referenced transaction.
 */
public record ReverseTransactionCommand(
        UUID refTransactionId,
        Instant occurredAt,
        Instant appliesAt
) {}
