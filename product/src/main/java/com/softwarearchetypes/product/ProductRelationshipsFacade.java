package com.softwarearchetypes.product;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.product.ProductRelationshipCommands.*;

import java.util.UUID;

public class ProductRelationshipsFacade {

    private final ProductRelationshipFactory factory;
    private final ProductRelationshipRepository repository;
    private final ProductTypeRepository productTypeRepository;

    ProductRelationshipsFacade(
            ProductRelationshipFactory factory,
            ProductRelationshipRepository repository,
            ProductTypeRepository productTypeRepository) {
        this.factory = factory;
        this.repository = repository;
        this.productTypeRepository = productTypeRepository;
    }

    // ============================================
    // Commands
    // ============================================

    /**
     * Defines a new relationship between two products.
     */
    public Result<String, ProductRelationshipId> handle(DefineRelationship command) {
        try {
            var from = parseProductIdentifier(command.fromProductId());
            var to = parseProductIdentifier(command.toProductId());
            var type = parseRelationshipType(command.relationshipType());

            if (productTypeRepository.findBy(from).isEmpty()) {
                return Result.failure("PRODUCT_NOT_FOUND: " + from.asString());
            }
            if (productTypeRepository.findBy(to).isEmpty()) {
                return Result.failure("PRODUCT_NOT_FOUND: " + to.asString());
            }

            return factory.defineFor(from, to, type)
                    .peekSuccess(repository::save)
                    .map(ProductRelationship::id);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Removes an existing relationship.
     */
    public Result<String, ProductRelationshipId> handle(RemoveRelationship command) {
        try {
            var relationshipId = ProductRelationshipId.of(UUID.fromString(command.relationshipId()));
            repository.delete(relationshipId);
            return Result.success(relationshipId);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // ============================================
    // Conversions: Simple types → Domain objects
    // ============================================

    private ProductIdentifier parseProductIdentifier(String value) {
        return UuidProductIdentifier.of(UUID.fromString(value));
    }

    private ProductRelationshipType parseRelationshipType(String type) {
        return ProductRelationshipType.valueOf(type.toUpperCase());
    }
}
