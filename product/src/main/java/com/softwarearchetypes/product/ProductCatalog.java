package com.softwarearchetypes.product;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.product.ProductCommands.*;
import com.softwarearchetypes.product.ProductQueries.*;
import com.softwarearchetypes.product.ProductViews.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ProductCatalog - main API for managing commercial product offering.
 * Accepts commands and queries with simple types, returns views.
 */
public class ProductCatalog {

    private final CatalogEntryRepository catalogRepository;
    private final ProductTypeRepository productTypeRepository;

    public ProductCatalog(CatalogEntryRepository catalogRepository, ProductTypeRepository productTypeRepository) {
        this.catalogRepository = catalogRepository;
        this.productTypeRepository = productTypeRepository;
    }

    public static ProductCatalog create(ProductTypeRepository productTypeRepository) {
        return new ProductCatalog(CatalogEntryRepository.inMemory(), productTypeRepository);
    }

    // ============================================
    // Commands
    // ============================================

    /**
     * Adds a ProductType to the commercial offer.
     */
    public Result<String, CatalogEntryId> handle(AddToOffer command) {
        try {
            // Verify ProductType exists
            var productType = productTypeRepository.findByIdValue(command.productTypeId())
                .orElseThrow(() -> new IllegalArgumentException("ProductType not found: " + command.productTypeId()));

            // Build validity from dates
            var validity = buildValidity(command.availableFrom(), command.availableUntil());

            // Generate CatalogEntryId
            var catalogEntryId = CatalogEntryId.generate();

            // Build CatalogEntry
            var catalogEntry = CatalogEntry.builder()
                .id(catalogEntryId)
                .displayName(command.displayName())
                .description(command.description())
                .productType(productType)
                .categories(command.categories())
                .validity(validity)
                .metadata(command.metadata())
                .build();

            catalogRepository.save(catalogEntry);

            return Result.success(catalogEntryId);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Discontinues a product from the offer by setting the validity end date.
     */
    public Result<String, CatalogEntryId> handle(DiscontinueProduct command) {
        try {
            var catalogEntryId = CatalogEntryId.of(command.catalogEntryId());
            var catalogEntry = catalogRepository.findById(catalogEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Catalog entry not found: " + command.catalogEntryId()));

            // Update validity to end at discontinuation date
            var newValidity = catalogEntry.validity().from() != null
                ? Validity.between(catalogEntry.validity().from(), command.discontinuationDate())
                : Validity.until(command.discontinuationDate());

            var updated = catalogEntry.withValidity(newValidity);
            catalogRepository.save(updated);

            return Result.success(catalogEntryId);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Updates catalog entry metadata.
     */
    public Result<String, CatalogEntryId> handle(UpdateMetadata command) {
        try {
            var catalogEntryId = CatalogEntryId.of(command.catalogEntryId());
            var catalogEntry = catalogRepository.findById(catalogEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Catalog entry not found: " + command.catalogEntryId()));

            var updated = catalogEntry.withMetadata(command.metadata());
            catalogRepository.save(updated);

            return Result.success(catalogEntryId);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // ============================================
    // Queries
    // ============================================

    /**
     * Searches catalog entries with multiple filters.
     */
    public Set<CatalogEntryView> findBy(SearchCatalogCriteria criteria) {
        var entries = catalogRepository.findAll();

        // Apply filters
        return entries.stream()
            .filter(entry -> matchesSearchText(entry, criteria.searchText()))
            .filter(entry -> matchesCategories(entry, criteria.categories()))
            .filter(entry -> matchesAvailability(entry, criteria.availableAt()))
            .filter(entry -> matchesProductType(entry, criteria.productTypeId()))
            .filter(entry -> matchesFeatures(entry, criteria.productTypeFeatures()))
            .map(this::toCatalogEntryView)
            .collect(Collectors.toSet());
    }

    /**
     * Finds a catalog entry by its identifier.
     */
    public Optional<CatalogEntryView> findBy(FindCatalogEntryCriteria criteria) {
        var catalogEntryId = CatalogEntryId.of(criteria.catalogEntryId());
        return catalogRepository.findById(catalogEntryId)
            .map(this::toCatalogEntryView);
    }

    /**
     * Finds catalog entries by category.
     */
    public Set<CatalogEntryView> findBy(FindByCategoryCriteria criteria) {
        return catalogRepository.findByCategory(criteria.category()).stream()
            .map(this::toCatalogEntryView)
            .collect(Collectors.toSet());
    }

    /**
     * Finds catalog entries available at specific date.
     */
    public Set<CatalogEntryView> findBy(FindAvailableAtCriteria criteria) {
        return catalogRepository.findAll().stream()
            .filter(entry -> entry.isAvailableAt(criteria.date()))
            .map(this::toCatalogEntryView)
            .collect(Collectors.toSet());
    }

    /**
     * Finds catalog entries by metadata key-value.
     */
    public Set<CatalogEntryView> findBy(FindByMetadataCriteria criteria) {
        return catalogRepository.findAll().stream()
            .filter(entry -> matchesMetadata(entry, criteria.key(), criteria.value()))
            .map(this::toCatalogEntryView)
            .collect(Collectors.toSet());
    }

    // ============================================
    // Filter helpers
    // ============================================

    private boolean matchesSearchText(CatalogEntry entry, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        var lowerSearch = searchText.toLowerCase();
        return entry.displayName().toLowerCase().contains(lowerSearch) ||
               entry.description().toLowerCase().contains(lowerSearch);
    }

    private boolean matchesCategories(CatalogEntry entry, Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return true;
        }
        return categories.stream().anyMatch(entry::isInCategory);
    }

    private boolean matchesAvailability(CatalogEntry entry, LocalDate date) {
        if (date == null) {
            return true;
        }
        return entry.isAvailableAt(date);
    }

    private boolean matchesProductType(CatalogEntry entry, String productTypeId) {
        if (productTypeId == null || productTypeId.isBlank()) {
            return true;
        }
        return entry.productType().id().toString().equals(productTypeId);
    }

    private boolean matchesFeatures(CatalogEntry entry, java.util.Map<String, Set<String>> features) {
        if (features == null || features.isEmpty()) {
            return true;
        }

        var productType = entry.productType();
        var allFeatures = new java.util.HashSet<>(productType.featureTypes().mandatoryFeatures());
        allFeatures.addAll(productType.featureTypes().optionalFeatures());

        // Check if all requested features match
        for (var featureEntry : features.entrySet()) {
            var featureName = featureEntry.getKey();
            var requestedValues = featureEntry.getValue();

            var feature = allFeatures.stream()
                .filter(f -> f.name().equals(featureName))
                .findFirst();

            if (feature.isEmpty()) {
                return false; // Feature not found in ProductType
            }

            // Check if any of the requested values is valid for this feature
            var featureType = feature.get();
            boolean anyValueMatches = requestedValues.stream()
                .anyMatch(value -> featureType.isValidValue(value));

            if (!anyValueMatches) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesMetadata(CatalogEntry entry, String key, String value) {
        if (value == null) {
            return entry.hasMetadata(key);
        }
        return value.equals(entry.getMetadata(key).orElse(null));
    }

    // ============================================
    // Conversions
    // ============================================

    private Validity buildValidity(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return Validity.between(from, to);
        } else if (from != null) {
            return Validity.from(from);
        } else if (to != null) {
            return Validity.until(to);
        } else {
            return Validity.always();
        }
    }

    private CatalogEntryView toCatalogEntryView(CatalogEntry entry) {
        return new CatalogEntryView(
            entry.id().value(),
            entry.displayName(),
            entry.description(),
            entry.productType().id().toString(),
            entry.categories(),
            entry.validity().from(),
            entry.validity().to(),
            entry.metadata()
        );
    }
}
