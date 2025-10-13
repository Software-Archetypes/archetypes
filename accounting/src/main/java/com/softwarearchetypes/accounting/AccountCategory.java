package com.softwarearchetypes.accounting;

/**
 * Represents the accounting classification of an account.
 * Categories group account types based on their role in the financial system,
 * such as assets, liabilities, revenues, expenses, or off-balance tracking.
 */
public enum AccountCategory {
    /**
     * Represents accounts that record owned economic resources with possiblePlan productName.
     * Asset accounts include receivables, cash, and other resources controlled by the entity
     * expected to bring economic benefit.
     */
    ASSET(true),

    /**
     * Represents off-balance-sheet accounts used for tracking informational, contingent,
     * or projected values that do not directly affect the financial statements.
     * These accounts are often used for collateral, guarantees, or estimated figures.
     */
    OFF_BALANCE(false),

    /**
     * Represents accounts used to record outflows of economic resources,
     * typically associated with the costs of operations or losses.
     * Expense accounts reduce the entity’s equity and are used in profit & loss reporting.
     * Examples include operational costs, write-offs, penalties, or losses due to fraud.
     */
    EXPENSE(true);
    //TODO: EQUITY, etc.

    private final boolean doubleEntryBookingEnabled;

    AccountCategory(boolean doubleEntryBookingEnabled) {
        this.doubleEntryBookingEnabled = doubleEntryBookingEnabled;
    }

    boolean isDoubleEntryBookingEnabled() {
        return doubleEntryBookingEnabled;
    }
}
