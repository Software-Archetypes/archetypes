package com.softwarearchetypes.product;

/**
 * Public API - commands for interacting with ProductRelationshipsFacade.
 * Commands represent write operations (mutations).
 * All fields use simple types - no domain objects leak through the API.
 */
public class ProductRelationshipCommands {

    private ProductRelationshipCommands() {
        // Static utility class
    }

    /**
     * Command to define a new relationship between two products.
     */
    public record DefineRelationship(
            String fromProductId,
            String toProductId,
            String relationshipType  // "UPGRADABLE_TO", "SUBSTITUTED_BY", etc.
    ) {
        public DefineRelationship {
            if (fromProductId == null || fromProductId.isBlank()) {
                throw new IllegalArgumentException("From product ID must be defined");
            }
            if (toProductId == null || toProductId.isBlank()) {
                throw new IllegalArgumentException("To product ID must be defined");
            }
            if (relationshipType == null || relationshipType.isBlank()) {
                throw new IllegalArgumentException("Relationship type must be defined");
            }
        }
    }

    /**
     * Command to remove an existing relationship.
     */
    public record RemoveRelationship(
            String relationshipId
    ) {
        public RemoveRelationship {
            if (relationshipId == null || relationshipId.isBlank()) {
                throw new IllegalArgumentException("Relationship ID must be defined");
            }
        }
    }
}
