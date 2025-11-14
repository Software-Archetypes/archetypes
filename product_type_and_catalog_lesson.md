# Lekcja: Product Type i Product Catalog

## Wstęp: Czym jest produkt?

**Produkt to coś, co wpływa na stan naszych zasobów.**

Zasoby możemy zdefiniować bardzo szeroko:
- Fizyczne zapasy (towary w magazynie)
- Stan kont bankowych
- Zobowiązania kontraktowe
- Dane i informacje
- Relacje z klientami
- Czas i dostępność personelu

**Produkt nie musi być "sprzedawany", żeby być produktem.**

Przykłady:
- **Lokata bankowa** - wpływa na stan środków klienta i zobowiązania banku
- **Konsultacja lekarska** - wpływa na dostępność lekarza i stan zdrowia pacjenta
- **Abonament telefoniczny** - wpływa na zobowiązania kontraktowe i limity usług
- **Usługa księgowa** - wpływa na czas księgowego i stan dokumentacji klienta

Nie wszystkie produkty pojawiają się w ofercie handlowej:
- Produkty wewnętrzne (np. alokacja zasobów, projekty)
- Produkty archwizowane (definicja pozostaje, ale nie są już oferowane)
- Produkty testowe (definiujemy, ale nie publikujemy)

Dlatego mamy **dwa archetypy**:
1. **ProductType** - definicja produktu (co to jest, jakie ma cechy)
2. **CatalogEntry** - pozycja w ofercie handlowej (co sprzedajemy, kiedy, w jakich kategoriach)

---

## Dwie Fasady: Różne Konteksty

### ProductFacade - Zarządzanie Definicjami Produktów

**Odpowiedzialność:**
- Definiowanie ProductType (czym produkt jest)
- Określanie cech produktowych (features)
- Zarządzanie strategią śledzenia (tracking strategy)
- Zarządzanie jednostkami miary

**Kto używa:**
- Product Managerowie
- Zespoły operacyjne
- Integracje systemowe (ERP, WMS, CRM)

**Przykład:**
Definiuję "Konto oszczędnościowe" jako ProductType - określam że ma cechy: minimalny wkład, okres wypowiedzenia, oprocentowanie. To definicja biznesowa produktu, niezależna od tego, czy go aktualnie oferujemy.

### ProductCatalog - Zarządzanie Ofertą Handlową

**Odpowiedzialność:**
- Publikowanie produktów w ofercie
- Określanie dostępności (validity)
- Kategoryzowanie dla klientów
- Dodawanie metadanych marketingowych (badges, promocje, priority)

**Kto używa:**
- Dział Marketingu
- Dział Sprzedaży
- Klienci (pośrednio przez frontend)

**Przykład:**
Dodaję "Konto oszczędnościowe" do katalogu z nazwą marketingową "Lokata Premium Q1", kategorią "Produkty oszczędnościowe", dostępnością od 01.01.2024 do 31.03.2024, i metadaną `{"badge": "7.5% oprocentowania", "campaign": "Q1-2024"}`.

---

## Separacja: ProductType ↔ CatalogEntry

### ProductType - "Czym produkt JEST"

```java
ProductType depositAccount = ProductType.builder(
    new UuidProductIdentifier(),
    ProductName.of("Konto oszczędnościowe"),
    ProductDescription.of("Rachunek terminowy z oprocentowaniem"),
    Unit.of("agreement", "agreement"),
    ProductTrackingStrategy.INDIVIDUALLY_TRACKED
)
.withMandatoryFeature(ProductFeatureType.withNumericRange("termInMonths", 1, 36))
.withMandatoryFeature(ProductFeatureType.withDecimalRange("minAmount", "1000", "1000000"))
.withOptionalFeature(ProductFeatureType.withAllowedValues("currency", "PLN", "EUR", "USD"))
.build();
```

**Kluczowe cechy:**
- Nazwa techniczna (`name`) - dla systemów wewnętrznych
- Opis operacyjny (`description`) - jak produkt działa
- Cechy produktowe (`features`) - co można konfigurować
- Strategia śledzenia - jak identyfikujemy konkretne instancje

**To nie zmienia się często** - definicja "Konta oszczędnościowego" jest stabilna.

### CatalogEntry - "Co SPRZEDAJEMY"

```java
CatalogEntry entry = CatalogEntry.builder()
    .id(CatalogEntryId.of("LOKATA-PREMIUM-Q1"))
    .displayName("Lokata Premium 7.5%")
    .description("Najwyższe oprocentowanie na rynku! Już od 1000 zł.")
    .productType(depositAccount)  // 1:1 relacja
    .categories(Set.of("Oszczędności", "Promocje", "Nowości"))
    .validity(Validity.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)))
    .metadata(Map.of(
        "badge", "7.5%",
        "campaign", "Q1-2024",
        "priority", "1",
        "featured", "true"
    ))
    .build();
```

**Kluczowe cechy:**
- Nazwa marketingowa (`displayName`) - dla klientów
- Opis sprzedażowy (`description`) - zachęta do zakupu
- Kategorie - dla nawigacji w UI
- Validity - kiedy dostępne
- Metadata - elastyczne atrybuty (promocje, badges, SEO)

**To zmienia się często** - kampanie, promocje, dostępność sezonowa.

---

## Relacja CatalogEntry ↔ ProductType: 1:1 czy 1:wiele?

**Nasza decyzja: 1 CatalogEntry = 1 ProductType**

### Dlaczego 1:1?

#### Prostota i klarowność
- Łatwiej zrozumieć i utrzymać
- Clear ownership: jeden wpis w katalogu = jedna definicja produktu
- Konsystencja danych

#### Różnice wyrażamy przez Features
Zamiast tworzyć wiele ProductType dla wariantów, używamy ProductFeatureType:

```java
// JEDEN ProductType z konfigurowalnymi cechami
ProductType iphone15Pro = ProductType.builder(...)
    .withMandatoryFeature(storageFeature)  // 128GB, 256GB, 512GB, 1TB
    .withMandatoryFeature(colorFeature)    // Space Black, Silver, Blue, Titanium
    .build();

// JEDEN CatalogEntry
CatalogEntry entry = CatalogEntry.builder()
    .displayName("iPhone 15 Pro")
    .productType(iphone15Pro)
    .build();
```

### Dwa pattern'y w UI

Ten sam model (1:1) obsługuje dwa różne sposoby prezentacji:

#### Pattern 1: Konfigurator (jedna karta produktu)

```
┌─────────────────────────────────────┐
│  [📱 iPhone 15 Pro]                 │
│                                     │
│  Wybierz pamięć:                    │
│  ○ 128GB (+0 zł)                    │
│  ○ 256GB (+800 zł)                  │
│  ○ 512GB (+2000 zł)                 │
│                                     │
│  Wybierz kolor:                     │
│  ⚫ Space Black  ⚪ Silver           │
│                                     │
│  [Dodaj do koszyka]                 │
└─────────────────────────────────────┘
```

**Backend:** 1 ProductType, 1 CatalogEntry
**Frontend:** Jedna strona z selectorami dla features
**ProductInstance:** Tworzony po wyborze klienta z konkretnymi wartościami (storage=256GB, color=Silver)

#### Pattern 2: Osobne kafelki

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ [📱 128GB]   │  │ [📱 256GB]   │  │ [📱 512GB]   │
│ 5499 zł      │  │ 6299 zł      │  │ 7499 zł      │
│ [Kup teraz]  │  │ [Kup teraz]  │  │ [Kup teraz]  │
└──────────────┘  └──────────────┘  └──────────────┘
```

**Backend:** Nadal 1 ProductType, 1 CatalogEntry
**Frontend:** Rekombinuje features i renderuje jako osobne kafelki
**Pricing Service:** Kalkuluje cenę dla każdej kombinacji features

### Kiedy tworzyć osobne ProductType?
re
**Twórz osobny ProductType gdy:**
1. Produkty różnią się biznesowo (różne SKU, dostawcy, koszty)
2. Potrzebujesz osobnej kontroli lifecycle (jeden wariant wycofany, inne aktywne)
3. Marketing wymaga osobnych kampanii/stron dla wariantów
4. Różne strategie śledzenia (jeden BATCH_TRACKED, inny INDIVIDUALLY_TRACKED)

**Przykład - Abonament telefoniczny:**

```java
// Osobne ProductTypes dla różnych poziomów
ProductType basicPlan = ProductType.builder(...)
    .name("Abonament Basic")
    .withMandatoryFeature(dataLimitFeature)  // 5GB, 10GB, 20GB
    .build();

ProductType premiumPlan = ProductType.builder(...)
    .name("Abonament Premium")
    .withMandatoryFeature(dataLimitFeature)  // 50GB, 100GB, unlimited
    .withOptionalFeature(roamingFeature)
    .build();

// Osobne CatalogEntry dla każdego planu
CatalogEntry basicEntry = CatalogEntry.builder()
    .displayName("Abonament Basic - Dla Oszczędnych")
    .productType(basicPlan)
    .categories(Set.of("Abonamenty", "Oferta podstawowa"))
    .build();

CatalogEntry premiumEntry = CatalogEntry.builder()
    .displayName("Abonament Premium - Bez Limitów")
    .productType(premiumPlan)
    .categories(Set.of("Abonamenty", "Premium", "Biznes"))
    .build();
```

**Dlaczego osobno?** Basic i Premium to różne poziomy usług, z różnymi cenami bazowymi, różnymi SLA, różnymi grupami docelowymi.

---

## Model: Kod i Struktura

### 1. ProductFacade - Zarządzanie ProductType

#### Komendy (write operations)

```java
public class ProductFacade {

    // Definiowanie nowego ProductType
    public Result<String, ProductIdentifier> handle(DefineProductType command) {
        // Parsowanie z DTO na domain objects
        var productId = parseProductIdentifier(command.productIdType(), command.productId());
        var productType = ProductType.builder(productId, ...)
            .withMandatoryFeature(...)
            .build();

        repository.save(productType);
        return Result.success(productId);
    }
}
```

**Command DTO:**

```java
public record DefineProductType(
    String productIdType,          // "UUID", "ISBN", "GTIN"
    String productId,
    String name,
    String description,
    String unit,                   // "pcs", "kg", "agreement"
    String trackingStrategy,       // "IDENTICAL", "INDIVIDUALLY_TRACKED"
    Set<MandatoryFeature> mandatoryFeatures,
    Set<OptionalFeature> optionalFeatures
) {}

public record MandatoryFeature(
    String name,
    FeatureConstraintConfig constraint  // Type-safe sealed interface
) {}
```

**Feature Constraint Configs (type-safe):**

```java
// Zamiast magicznych Map<String, Object> mamy dedykowane typy:
new AllowedValuesConfig(Set.of("S", "M", "L", "XL"))
new NumericRangeConfig(1, 100)
new DecimalRangeConfig("0.5", "999.99")
new RegexConfig("^[A-Z]{2}-\\d{4}$")
new DateRangeConfig("2024-01-01", "2024-12-31")
new UnconstrainedConfig("TEXT")
```

#### Queries (read operations)

```java
// Szukanie po ID (string, bez podawania typu)
public Optional<ProductTypeView> findBy(FindProductTypeCriteria criteria) {
    return repository.findByIdValue(criteria.productId())
        .map(this::toProductTypeView);
}

// Szukanie po tracking strategy
public Set<ProductTypeView> findBy(FindByTrackingStrategyCriteria criteria) {
    var strategy = parseTrackingStrategy(criteria.trackingStrategy());
    return repository.findByTrackingStrategy(strategy).stream()
        .map(this::toProductTypeView)
        .collect(Collectors.toSet());
}
```

**View DTO:**

```java
public record ProductTypeView(
    String productId,
    String name,
    String description,
    String unit,
    String trackingStrategy,
    Set<FeatureTypeView> mandatoryFeatures,
    Set<FeatureTypeView> optionalFeatures
) {}
```

### 2. ProductCatalog - Zarządzanie Ofertą

#### Komendy

```java
public class ProductCatalog {

    // Dodawanie do oferty
    public Result<String, CatalogEntryId> handle(AddToOffer command) {
        // Walidacja: ProductType musi istnieć
        var productType = productTypeRepository.findByIdValue(command.productTypeId())
            .orElseThrow(() -> new IllegalArgumentException("ProductType not found"));

        var catalogEntry = CatalogEntry.builder()
            .id(CatalogEntryId.generate())
            .displayName(command.displayName())
            .productType(productType)
            .validity(buildValidity(command.availableFrom(), command.availableUntil()))
            .build();

        catalogRepository.save(catalogEntry);
        return Result.success(catalogEntry.id());
    }

    // Wycofywanie z oferty
    public Result<String, CatalogEntryId> handle(DiscontinueProduct command) {
        var entry = catalogRepository.findById(...)
            .orElseThrow(...);

        var newValidity = Validity.until(command.discontinuationDate());
        var updated = entry.withValidity(newValidity);

        catalogRepository.save(updated);
        return Result.success(entry.id());
    }
}
```

#### Queries

```java
// Złożone wyszukiwanie
public Set<CatalogEntryView> findBy(SearchCatalogCriteria criteria) {
    return catalogRepository.findAll().stream()
        .filter(entry -> matchesSearchText(entry, criteria.searchText()))
        .filter(entry -> matchesCategories(entry, criteria.categories()))
        .filter(entry -> matchesAvailability(entry, criteria.availableAt()))
        .filter(entry -> matchesProductType(entry, criteria.productTypeId()))
        .filter(entry -> matchesFeatures(entry, criteria.productTypeFeatures()))
        .map(this::toCatalogEntryView)
        .collect(Collectors.toSet());
}
```

**Search Criteria - elastyczne filtry:**

```java
public record SearchCatalogCriteria(
    String searchText,                        // Wyszukiwan/loginie w nazwie i opisie
    Set<String> categories,                   // Filtrowanie po kategoriach
    LocalDate availableAt,                    // Dostępne na konkretną datę
    String productTypeId,                     // Konkretny ProductType
    Map<String, Set<String>> productTypeFeatures  // Filtrowanie po features ProductType
) {
    // Factory methods dla wygody
    public static SearchCatalogCriteria all() { ... }
    public static SearchCatalogCriteria byText(String text) { ... }
    public static SearchCatalogCriteria byFeatures(Map<String, Set<String>> features) { ... }
}
```

**Wyszukiwanie po features ProductType:**

```java
// Znajdź produkty które mają storage 256GB lub 512GB
var criteria = SearchCatalogCriteria.byFeatures(
    Map.of("storage", Set.of("256GB", "512GB"))
);

Set<CatalogEntryView> results = catalog.findBy(criteria);
```

Implementacja:

```java
private boolean matchesFeatures(CatalogEntry entry, Map<String, Set<String>> features) {
    if (features == null || features.isEmpty()) return true;

    var productType = entry.productType();
    var allFeatures = new HashSet<>(productType.featureTypes().mandatoryFeatures());
    allFeatures.addAll(productType.featureTypes().optionalFeatures());

    // Dla każdej żądanej cechy
    for (var featureEntry : features.entrySet()) {
        var featureName = featureEntry.getKey();
        var requestedValues = featureEntry.getValue();

        var feature = allFeatures.stream()
            .filter(f -> f.name().equals(featureName))
            .findFirst();

        if (feature.isEmpty()) return false;  // ProductType nie ma tej cechy

        // Czy któraś z żądanych wartości jest valid dla constraint?
        boolean anyValueMatches = requestedValues.stream()
            .anyMatch(value -> feature.get().isValidValue(value));

        if (!anyValueMatches) return false;
    }

    return true;
}
```

---

## Przykłady Użycia: Różne Branże

### 1. Bankowość - Produkty Depozytowe

#### Definicja ProductType

```java
var facade = ProductFacade.create();

// Lokata terminowa
var result = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "Lokata terminowa standard",
    "Rachunek terminowy z oprocentowaniem stałym",
    "agreement",
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "termInMonths",
            new NumericRangeConfig(1, 36)  // 1-36 miesięcy
        ),
        new MandatoryFeature(
            "minAmount",
            new DecimalRangeConfig("1000.00", "10000000.00")  // 1k - 10M PLN
        ),
        new MandatoryFeature(
            "currency",
            new AllowedValuesConfig(Set.of("PLN", "EUR", "USD"))
        )
    ),
    Set.of(
        new OptionalFeature(
            "autoRenewal",
            new AllowedValuesConfig(Set.of("YES", "NO"))
        )
    )
));
```

#### Dodanie do Oferty (różne kampanie)

```java
var catalog = ProductCatalog.create(productTypeRepository);

// Kampania Q1 - wyższe oprocentowanie
catalog.handle(new AddToOffer(
    result.getValue().toString(),
    "Lokata Premium 7.5% - Oferta Specjalna",
    "Oprocentowanie 7.5% w skali roku. Minimalny wkład 10 000 zł.",
    Set.of("Lokaty", "Promocje", "Oszczędności"),
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2024, 3, 31),
    Map.of(
        "badge", "7.5%",
        "campaign", "Q1-2024",
        "interestRate", "7.5",
        "minAmount", "10000",
        "featured", "true"
    )
));

// Kampania Q2 - standardowe oprocentowanie
catalog.handle(new AddToOffer(
    result.getValue().toString(),
    "Lokata Oszczędnościowa 6.0%",
    "Bezpieczna lokata z oprocentowaniem 6.0% w skali roku.",
    Set.of("Lokaty", "Oszczędności"),
    LocalDate.of(2024, 4, 1),
    LocalDate.of(2024, 6, 30),
    Map.of(
        "badge", "Sprawdzona",
        "campaign", "Q2-2024",
        "interestRate", "6.0",
        "minAmount", "1000"
    )
));
```

**Ten sam ProductType, różne wpisy w katalogu** - różne okresy dostępności, różne nazwy marketingowe, różne metadane (oprocentowanie, minimalny wkład).

#### Wyszukiwanie dla Klienta

```java
// Klient szuka lokat dostępnych teraz, w walucie PLN
var searchCriteria = new SearchCatalogCriteria(
    "lokata",                          // searchText
    Set.of("Lokaty", "Promocje"),      // categories
    LocalDate.now(),                   // availableAt
    null,                              // productTypeId - dowolny
    Map.of("currency", Set.of("PLN"))  // features - tylko PLN
);

Set<CatalogEntryView> availableDeposits = catalog.findBy(searchCriteria);

// Wynik: Tylko lokaty dostępne teraz, z walutą PLN w features
```

### 2. Telekomunikacja - Plany Abonamentowe

#### Definicja ProductType

```java
// Abonament Basic
var basicPlanResult = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "Mobile Plan Basic",
    "Podstawowy plan abonamentowy z limitami",
    "agreement",
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "dataLimitGB",
            new NumericRangeConfig(5, 20)  // 5GB, 10GB, 20GB
        ),
        new MandatoryFeature(
            "voiceMinutes",
            new AllowedValuesConfig(Set.of("500", "1000", "UNLIMITED"))
        ),
        new MandatoryFeature(
            "smsCount",
            new AllowedValuesConfig(Set.of("100", "500", "UNLIMITED"))
        )
    ),
    Set.of(
        new OptionalFeature(
            "internationalRoaming",
            new AllowedValuesConfig(Set.of("EU", "EU_US", "WORLDWIDE"))
        )
    )
));

// Abonament Premium
var premiumPlanResult = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "Mobile Plan Premium",
    "Plan premium z nieograniczonymi zasobami",
    "agreement",
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "dataLimitGB",
            new AllowedValuesConfig(Set.of("50", "100", "UNLIMITED"))
        ),
        new MandatoryFeature(
            "voiceMinutes",
            new AllowedValuesConfig(Set.of("UNLIMITED"))
        )
    ),
    Set.of(
        new OptionalFeature(
            "5GAccess",
            new AllowedValuesConfig(Set.of("YES", "NO"))
        ),
        new OptionalFeature(
            "internationalRoaming",
            new AllowedValuesConfig(Set.of("EU", "WORLDWIDE"))
        )
    )
));
```

#### Dodanie do Oferty

```java
// Basic w ofercie podstawowej
catalog.handle(new AddToOffer(
    basicPlanResult.getValue().toString(),
    "Plan Podstawowy - Od 29.99 zł/mies.",
    "Idealne rozwiązanie dla oszczędnych. Do 20GB internetu i nielimitowane rozmowy.",
    Set.of("Abonamenty", "Dla każdego"),
    LocalDate.now(),
    null,  // Dostępny na zawsze
    Map.of(
        "basePrice", "29.99",
        "targetAudience", "personal",
        "priority", "2"
    )
));

// Premium w ofercie biznesowej
catalog.handle(new AddToOffer(
    premiumPlanResult.getValue().toString(),
    "Plan Biznes Premium - Bez Limitów",
    "Wszystko bez limitów + 5G. Roaming w całej Europie w cenie.",
    Set.of("Abonamenty", "Biznes", "Premium"),
    LocalDate.now(),
    null,
    Map.of(
        "basePrice", "99.99",
        "targetAudience", "business",
        "badge", "Najpopularniejszy",
        "priority", "1",
        "featured", "true"
    )
));
```

#### Wyszukiwanie

```java
// Klient biznesowy szuka planów z unlimited data
var businessCriteria = new SearchCatalogCriteria(
    null,
    Set.of("Biznes"),
    LocalDate.now(),
    null,
    Map.of("dataLimitGB", Set.of("UNLIMITED"))
);

Set<CatalogEntryView> unlimitedPlans = catalog.findBy(businessCriteria);
```

### 3. Usługi Medyczne - Konsultacje

#### Definicja ProductType

```java
// Konsultacja specjalistyczna
var consultationResult = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "Medical Consultation",
    "Konsultacja ze specjalistą medycznym",
    "h",  // godziny
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "specialization",
            new AllowedValuesConfig(Set.of(
                "Cardiology", "Dermatology", "Neurology", "Orthopedics"
            ))
        ),
        new MandatoryFeature(
            "duration",
            new AllowedValuesConfig(Set.of("15", "30", "60"))  // minuty
        )
    ),
    Set.of(
        new OptionalFeature(
            "consultationType",
            new AllowedValuesConfig(Set.of("IN_PERSON", "ONLINE", "HOME_VISIT"))
        ),
        new OptionalFeature(
            "language",
            new AllowedValuesConfig(Set.of("PL", "EN", "DE"))
        )
    )
));

// Badanie diagnostyczne
var diagnosticResult = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "Diagnostic Test",
    "Badanie diagnostyczne",
    "pcs",
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "testType",
            new AllowedValuesConfig(Set.of(
                "BLOOD_TEST", "XRAY", "MRI", "CT_SCAN", "ULTRASOUND"
            ))
        )
    ),
    Set.of(
        new OptionalFeature(
            "urgency",
            new AllowedValuesConfig(Set.of("REGULAR", "EXPRESS", "EMERGENCY"))
        )
    )
));
```

#### Dodanie do Oferty (różne lokalizacje/cenniki)

```java
// Konsultacje w Warszawie
catalog.handle(new AddToOffer(
    consultationResult.getValue().toString(),
    "Konsultacja Specjalistyczna - Warszawa Centrum",
    "Wizyty u najlepszych specjalistów w centrum Warszawy.",
    Set.of("Konsultacje", "Warszawa", "Specjaliści"),
    LocalDate.now(),
    null,
    Map.of(
        "location", "Warsaw-Center",
        "basePrice", "250",
        "availableSpecializations", "Cardiology,Dermatology,Neurology",
        "bookingUrl", "https://example.com/booking/warsaw"
    )
));

// Konsultacje online - tańsze
catalog.handle(new AddToOffer(
    consultationResult.getValue().toString(),
    "Konsultacja Online - Wszędzie",
    "Konsultacja online z domu. Wygodnie i bezpiecznie.",
    Set.of("Konsultacje", "Online", "Promocja"),
    LocalDate.now(),
    null,
    Map.of(
        "location", "Online",
        "basePrice", "150",
        "badge", "Taniej o 40%",
        "featured", "true"
    )
));
```

#### Wyszukiwanie

```java
// Pacjent szuka konsultacji kardiologicznych dostępnych online
var searchCriteria = new SearchCatalogCriteria(
    "kardiolog",
    Set.of("Online", "Konsultacje"),
    LocalDate.now(),
    null,
    Map.of(
        "specialization", Set.of("Cardiology"),
        "consultationType", Set.of("ONLINE")
    )
);

Set<CatalogEntryView> onlineCardiology = catalog.findBy(searchCriteria);
```

### 4. Usługi IT - Hosting i Cloud

#### Definicja ProductType

```java
// VPS Server
var vpsResult = facade.handle(new DefineProductType(
    "UUID",
    UUID.randomUUID().toString(),
    "VPS Server",
    "Virtual Private Server z dedykowanymi zasobami",
    "pcs",
    "INDIVIDUALLY_TRACKED",
    Set.of(
        new MandatoryFeature(
            "cpuCores",
            new NumericRangeConfig(1, 32)
        ),
        new MandatoryFeature(
            "ramGB",
            new NumericRangeConfig(1, 128)
        ),
        new MandatoryFeature(
            "storageGB",
            new NumericRangeConfig(20, 2000)
        ),
        new MandatoryFeature(
            "os",
            new AllowedValuesConfig(Set.of("Ubuntu", "Debian", "CentOS", "Windows"))
        )
    ),
    Set.of(
        new OptionalFeature(
            "backupFrequency",
            new AllowedValuesConfig(Set.of("DAILY", "WEEKLY", "MONTHLY"))
        ),
        new OptionalFeature(
            "monitoring",
            new AllowedValuesConfig(Set.of("BASIC", "ADVANCED"))
        )
    )
));
```

#### Dodanie do Oferty (różne tier'y)

```java
// Starter tier
catalog.handle(new AddToOffer(
    vpsResult.getValue().toString(),
    "VPS Starter - Dla Małych Projektów",
    "2 CPU, 4GB RAM, 50GB SSD. Idealny na start.",
    Set.of("Hosting", "VPS", "Starter"),
    LocalDate.now(),
    null,
    Map.of(
        "tier", "starter",
        "basePrice", "29",
        "defaultCPU", "2",
        "defaultRAM", "4",
        "defaultStorage", "50"
    )
));

// Business tier
catalog.handle(new AddToOffer(
    vpsResult.getValue().toString(),
    "VPS Business - Dla Wymagających",
    "8 CPU, 16GB RAM, 500GB SSD. Monitoring i backup w cenie.",
    Set.of("Hosting", "VPS", "Business", "Polecane"),
    LocalDate.now(),
    null,
    Map.of(
        "tier", "business",
        "basePrice", "149",
        "defaultCPU", "8",
        "defaultRAM", "16",
        "defaultStorage", "500",
        "badge", "Najczęściej wybierany",
        "featured", "true"
    )
));
```

---

## Wnioski i Best Practices

### 1. Kiedy używać ProductFacade vs ProductCatalog?

**ProductFacade:**
- Definiowanie nowych typów produktów
- Zarządzanie definicjami biznesowymi
- Integracje systemowe (gdy inne systemy pytają "co to za produkt?")

**ProductCatalog:**
- Publikowanie w ofercie
- Zarządzanie dostępnością sezonową/kampaniami
- Frontend e-commerce / portale klientów

### 2. Jednoznaczność Responsibility

```
ProductType:        "Co to jest?"
CatalogEntry:       "Czy to sprzedajemy?"
ProductInstance:    "Konkretna sztuka/umowa"
```

Nie mieszaj tych odpowiedzialności.

### 3. Relacja 1:1 jako default

Zacznij od 1:1 (jeden CatalogEntry = jeden ProductType). Dopiero gdy biznesowo różnice są na tyle duże, twórz osobne ProductType.

**Pytanie pomocnicze:** "Czy to faktycznie dwa różne produkty z punktu widzenia operacji i logistyki?"

### 4. Features dla elastyczności

Zamiast mnożyć ProductType, używaj ProductFeatureType:
- Łatwiej zarządzać
- Mniej duplikacji kodu
- Jednorodne API dla klienta

### 5. Metadata dla zmienności

CatalogEntry.metadata to miejsce na dane, które zmieniają się często:
- Promocje, badges, kampanie
- Priorytety w UI
- SEO tags
- Temporary flags

Nie twórz dla tego dedykowanych pól - metadata daje elastyczność.

### 6. Type-safe Commands

Używaj dedykowanych DTO zamiast `Map<String, Object>`:

```java
// ❌ Słabo - magiczne stringi
Map.of("allowedValues", Set.of("S", "M", "L"))

// ✅ Dobrze - type-safe
new AllowedValuesConfig(Set.of("S", "M", "L"))
```

---

## Podsumowanie

**ProductType** to archetyp dla definicji produktu - czym produkt jest, jakie ma cechy, jak go śledzimy.

**CatalogEntry** to archetyp dla oferty handlowej - co sprzedajemy, kiedy, w jakich kategoriach, z jakim przekazem marketingowym.

**Dwie fasady** (ProductFacade, ProductCatalog) zapewniają separację kontekstów - definicje produktów vs oferta handlowa.

**Relacja 1:1** upraszcza model, a różnice wyrażamy przez ProductFeatureType i metadata.

**Produkt to coś, co wpływa na zasoby** - nie musi być sprzedawane, żeby być produktem. Dlatego ProductType istnieje niezależnie od CatalogEntry.
