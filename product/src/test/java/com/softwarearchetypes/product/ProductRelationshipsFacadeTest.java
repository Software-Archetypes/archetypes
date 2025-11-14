package com.softwarearchetypes.product;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.product.ProductRelationshipCommands.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRelationshipsFacadeTest {

    private final InMemoryProductTypeRepository productTypeRepository = new InMemoryProductTypeRepository();
    private final ProductRelationshipFactory factory = new ProductRelationshipFactory(ProductRelationshipId::random);
    private final InMemoryProductRelationshipRepository relationshipRepository = new InMemoryProductRelationshipRepository();
    private final ProductRelationshipsFacade facade = new ProductRelationshipsFacade(factory, relationshipRepository, productTypeRepository);
    private final ProductRelationshipsQueries queries = new ProductRelationshipsQueries(relationshipRepository);

    @Test
    void shouldFailToDefineRelationshipWhenFromProductDoesNotExist() {
        //given
        ProductIdentifier nonExistingFrom = UuidProductIdentifier.random();
        ProductType existingTo = thereIsProduct();

        //when
        Result<String, ProductRelationshipId> result = facade.handle(
                new DefineRelationship(
                        nonExistingFrom.asString(),
                        existingTo.identifier().asString(),
                        "UPGRADABLE_TO"
                )
        );

        //then
        assertTrue(result.failure());
        assertTrue(result.getFailure().contains("PRODUCT_NOT_FOUND"));
    }

    @Test
    void shouldFailToDefineRelationshipWhenToProductDoesNotExist() {
        //given
        ProductType existingFrom = thereIsProduct();
        ProductIdentifier nonExistingTo = UuidProductIdentifier.random();

        //when
        Result<String, ProductRelationshipId> result = facade.handle(
                new DefineRelationship(
                        existingFrom.identifier().asString(),
                        nonExistingTo.asString(),
                        "UPGRADABLE_TO"
                )
        );

        //then
        assertTrue(result.failure());
        assertTrue(result.getFailure().contains("PRODUCT_NOT_FOUND"));
    }

    @Test
    void shouldDefineRelationshipBetweenProducts() {
        //given
        ProductType smallCoffee = thereIsProduct();
        ProductType largeCoffee = thereIsProduct();

        //when
        Result<String, ProductRelationshipId> result = facade.handle(
                new DefineRelationship(
                        smallCoffee.identifier().asString(),
                        largeCoffee.identifier().asString(),
                        "UPGRADABLE_TO"
                )
        );

        //then
        assertTrue(result.success());
        ProductRelationshipId relationshipId = result.getSuccess();
        ProductRelationship relationship = queries.findBy(relationshipId).orElseThrow();
        assertEquals(smallCoffee.identifier(), relationship.from());
        assertEquals(largeCoffee.identifier(), relationship.to());
        assertEquals(ProductRelationshipType.UPGRADABLE_TO, relationship.type());
    }

    @Test
    void shouldRemoveRelationshipBetweenProducts() {
        //given
        ProductType smallCoffee = thereIsProduct();
        ProductType largeCoffee = thereIsProduct();
        ProductRelationshipId relationshipId = facade.handle(
                        new DefineRelationship(
                                smallCoffee.identifier().asString(),
                                largeCoffee.identifier().asString(),
                                "UPGRADABLE_TO"
                        )
                )
                .getSuccess();

        //when
        Result<String, ProductRelationshipId> result = facade.handle(
                new RemoveRelationship(relationshipId.asString())
        );

        //then
        assertTrue(result.success());
        assertTrue(queries.findBy(relationshipId).isEmpty());
    }

    @Test
    void shouldFindAllRelationsFromProduct() {
        //given
        ProductType burger = thereIsProduct();
        ProductType fries = thereIsProduct();
        ProductType coke = thereIsProduct();

        facade.handle(new DefineRelationship(
                burger.identifier().asString(),
                fries.identifier().asString(),
                "COMPLEMENTED_BY"
        ));
        facade.handle(new DefineRelationship(
                burger.identifier().asString(),
                coke.identifier().asString(),
                "COMPLEMENTED_BY"
        ));

        //when
        var relations = queries.findAllRelationsFrom(burger.identifier(), ProductRelationshipType.COMPLEMENTED_BY);

        //then
        assertEquals(2, relations.size());
        assertTrue(relations.stream().allMatch(rel -> rel.from().equals(burger.identifier())));
        assertTrue(relations.stream().allMatch(rel -> rel.type() == ProductRelationshipType.COMPLEMENTED_BY));
    }

    private ProductType thereIsProduct() {
        ProductType productType = ProductType.define(
                UuidProductIdentifier.random(),
                ProductName.of("Test Product"),
                ProductDescription.of("Description")
        );
        productTypeRepository.save(productType);
        return productType;
    }
}
