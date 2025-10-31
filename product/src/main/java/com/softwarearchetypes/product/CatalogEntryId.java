package com.softwarearchetypes.product;

import java.util.Objects;
import java.util.UUID;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Unique identifier for a CatalogEntry (may differ from ProductIdentifier).
 *
 * The same ProductType can appear in multiple catalog entries:
 * - Different marketing campaigns
 * - Different markets/regions
 * - Different time periods
 */
class CatalogEntryId {

    private final String value;

    private CatalogEntryId(String value) {
        checkArgument(value != null && !value.isBlank(), "CatalogEntryId must be defined");
        this.value = value;
    }

    static CatalogEntryId of(String value) {
        return new CatalogEntryId(value);
    }

    static CatalogEntryId generate() {
        return new CatalogEntryId("CATALOG-" + UUID.randomUUID());
    }

    String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogEntryId that = (CatalogEntryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
