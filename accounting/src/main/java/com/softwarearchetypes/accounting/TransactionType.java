package com.softwarearchetypes.accounting;

import com.fasterxml.jackson.annotation.JsonValue;

//payment_received, payment_sent, accrual, internal_transfer, provision, reversal, opening_balance
//todo: maybe enum
public record TransactionType(@JsonValue String value) {

    public static final TransactionType INITIALIZATION = new TransactionType("initialization");
    public static final TransactionType REVERSAL = new TransactionType("reversal");
    public static final TransactionType TRANSFER = new TransactionType("transfer");
    public static final TransactionType REALLOCATION = new TransactionType("reallocation");
    public static final TransactionType EXPIRATION_COMPENSATION = new TransactionType("expiration_compensation");

    static TransactionType of(String value) {
        return new TransactionType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
