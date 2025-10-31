package com.softwarearchetypes.product;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.product.ProductCommands.*;
import com.softwarearchetypes.product.ProductQueries.*;
import com.softwarearchetypes.product.ProductViews.*;
import com.softwarearchetypes.quantity.Unit;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ProductFacade - main API for managing ProductTypes.
 * Accepts commands and queries with simple types, returns views.
 */
public class ProductFacade {

    private final ProductTypeRepository repository;

    public ProductFacade(ProductTypeRepository repository) {
        this.repository = repository;
    }

    public static ProductFacade create() {
        return new ProductFacade(ProductTypeRepository.inMemory());
    }

    // ============================================
    // Commands
    // ============================================

    /**
     * Defines a new ProductType in the system.
     */
    public Result<String, ProductIdentifier> handle(DefineProductType command) {
        try {
            // Parse domain objects from simple types
            var productId = parseProductIdentifier(command.productIdType(), command.productId());
            var name = ProductName.of(command.name());
            var description = ProductDescription.of(command.description());
            var unit = parseUnit(command.unit());
            var trackingStrategy = parseTrackingStrategy(command.trackingStrategy());

            // Build ProductType
            var builder = ProductType.builder(productId, name, description, unit, trackingStrategy);

            // Add mandatory features
            if (command.mandatoryFeatures() != null) {
                for (var feature : command.mandatoryFeatures()) {
                    builder.withMandatoryFeature(toProductFeatureType(feature.name(), feature));
                }
            }

            // Add optional features
            if (command.optionalFeatures() != null) {
                for (var feature : command.optionalFeatures()) {
                    builder.withOptionalFeature(toProductFeatureType(feature.name(), feature));
                }
            }

            var productType = builder.build();
            repository.save(productType);

            return Result.success(productId);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // ============================================
    // Queries
    // ============================================

    /**
     * Finds a ProductType by its identifier.
     */
    public Optional<ProductTypeView> findBy(FindProductTypeCriteria criteria) {
        return repository.findByIdValue(criteria.productId())
            .map(this::toProductTypeView);
    }

    /**
     * Finds ProductTypes by tracking strategy.
     */
    public Set<ProductTypeView> findBy(FindByTrackingStrategyCriteria criteria) {
        var strategy = parseTrackingStrategy(criteria.trackingStrategy());
        return repository.findByTrackingStrategy(strategy).stream()
            .map(this::toProductTypeView)
            .collect(Collectors.toSet());
    }

    // ============================================
    // Conversions: Simple types → Domain objects
    // ============================================

    private ProductIdentifier parseProductIdentifier(String type, String value) {
        return switch (type.toUpperCase()) {
            case "UUID" -> UuidProductIdentifier.of(value);
            case "ISBN" -> IsbnProductIdentifier.of(value);
            case "GTIN" -> GtinProductIdentifier.of(value);
            default -> throw new IllegalArgumentException("Unknown product identifier type: " + type +
                ". Supported types: UUID, ISBN, GTIN");
        };
    }

    private ProductTrackingStrategy parseTrackingStrategy(String value) {
        return ProductTrackingStrategy.valueOf(value.toUpperCase());
    }

    private Unit parseUnit(String symbol) {
        // Map common symbols to predefined units
        return switch (symbol.toLowerCase()) {
            case "pcs", "pieces" -> Unit.pieces();
            case "kg", "kilograms" -> Unit.kilograms();
            case "l", "liters" -> Unit.liters();
            case "m", "meters" -> Unit.meters();
            case "m²", "m2", "square meters" -> Unit.squareMeters();
            case "m³", "m3", "cubic meters" -> Unit.cubicMeters();
            case "h", "hours" -> Unit.hours();
            case "min", "minutes" -> Unit.minutes();
            // For unknown symbols, create a new unit with symbol as both symbol and name
            default -> Unit.of(symbol, symbol);
        };
    }

    private ProductFeatureType toProductFeatureType(String name, MandatoryFeature feature) {
        var constraint = toConstraint(feature.constraint());
        return ProductFeatureType.of(name, constraint);
    }

    private ProductFeatureType toProductFeatureType(String name, OptionalFeature feature) {
        var constraint = toConstraint(feature.constraint());
        return ProductFeatureType.of(name, constraint);
    }

    private FeatureValueConstraint toConstraint(ProductCommands.FeatureConstraintConfig config) {
        return switch (config) {
            case ProductCommands.AllowedValuesConfig c ->
                AllowedValuesConstraint.of(c.allowedValues().toArray(new String[0]));
            case ProductCommands.NumericRangeConfig c ->
                NumericRangeConstraint.between(c.min(), c.max());
            case ProductCommands.DecimalRangeConfig c ->
                DecimalRangeConstraint.of(c.min(), c.max());
            case ProductCommands.RegexConfig c ->
                RegexConstraint.of(c.pattern());
            case ProductCommands.DateRangeConfig c ->
                DateRangeConstraint.between(c.from(), c.to());
            case ProductCommands.UnconstrainedConfig c -> {
                var type = FeatureValueType.valueOf(c.valueType().toUpperCase());
                yield new Unconstrained(type);
            }
        };
    }

    // ============================================
    // Conversions: Domain objects → Views
    // ============================================

    private ProductTypeView toProductTypeView(ProductType productType) {
        return new ProductTypeView(
            productType.id().toString(),
            productType.name().value(),
            productType.description().value(),
            productType.preferredUnit().symbol(),
            productType.trackingStrategy().name(),
            toFeatureTypeViews(productType.featureTypes().mandatoryFeatures()),
            toFeatureTypeViews(productType.featureTypes().optionalFeatures())
        );
    }

    private Set<FeatureTypeView> toFeatureTypeViews(Set<ProductFeatureType> features) {
        return features.stream()
            .map(this::toFeatureTypeView)
            .collect(Collectors.toSet());
    }

    private FeatureTypeView toFeatureTypeView(ProductFeatureType featureType) {
        var constraint = featureType.constraint();
        return new FeatureTypeView(
            featureType.name(),
            constraint.valueType().name(),
            constraint.type(),
            constraintConfigToMap(constraint),
            constraint.desc()
        );
    }

    private java.util.Map<String, Object> constraintConfigToMap(FeatureValueConstraint constraint) {
        // Extract configuration from constraint for API consumers
        return switch (constraint) {
            case AllowedValuesConstraint c -> Map.of("allowedValues", c.allowedValues());
            case NumericRangeConstraint c -> Map.of("min", c.min(), "max", c.max());
            case DecimalRangeConstraint c -> Map.of("min", c.min(), "max", c.max());
            case RegexConstraint c -> Map.of("pattern", c.pattern());
            case DateRangeConstraint c -> Map.of("from", c.from().toString(), "to", c.to().toString());
            case Unconstrained c -> Map.of();
            default -> Map.of();
        };
    }
}
