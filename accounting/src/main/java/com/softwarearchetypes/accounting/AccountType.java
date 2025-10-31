package com.softwarearchetypes.accounting;

/**
 * Represents the accounting classification of an account.
 * Account types group accounts based on their role in the financial system,
 * such as assets, liabilities, revenues, expenses, or off-balance tracking.
 */
public enum AccountType {
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
     * Expense accounts reduce the entity's equity and are used in profit & loss reporting.
     * Examples include operational costs, write-offs, penalties, or losses due to fraud.
     */
    EXPENSE(true),

    /**
     * Represents accounts that record obligations or debts owed by the entity.
     * Liability accounts include payables, loans, accrued expenses, and other financial
     * obligations that the entity must settle in the future.
     */
    LIABILITY(true),

    /**
     * Represents accounts used to record inflows of economic resources,
     * typically associated with sales, services rendered, or other income-generating activities.
     * Revenue accounts increase the entity's equity and are used in profit & loss reporting.
     */
    REVENUE(true);

    //TODO: EQUITY, etc.

    private final boolean doubleEntryBookingEnabled;

    AccountType(boolean doubleEntryBookingEnabled) {
        this.doubleEntryBookingEnabled = doubleEntryBookingEnabled;
    }

    boolean isDoubleEntryBookingEnabled() {
        return doubleEntryBookingEnabled;
    }
}
