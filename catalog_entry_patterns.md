# Relacja CatalogEntry ↔ ProductType: Jeden do jednego

## Problem: Jak modelować warianty produktów?

W e-commerce często spotykamy produkty z wariantami. Przykład: iPhone 15 Pro ma różne pojemności pamięci (128GB, 256GB, 512GB, 1TB) i kolory.

Jak to pokazać w UI? Jak to zamodelować?

### Pattern 1: Jedna karta produktu z konfiguratorem

**Jak wygląda w UI:**
```
┌─────────────────────────────────────┐
│  [📱 Zdjęcie iPhone 15 Pro]         │
│                                     │
│  iPhone 15 Pro                      │
│  od 5499 zł                         │
│                                     │
│  Wybierz pamięć:                    │
│  ○ 128GB (+0 zł)                    │
│  ○ 256GB (+800 zł)                  │
│  ○ 512GB (+2000 zł)                 │
│  ○ 1TB (+3500 zł)                   │
│                                     │
│  Wybierz kolor:                     │
│  ⚫ Space Black  ⚪ Silver           │
│  🔵 Blue  ⭐ Titanium               │
│                                     │
│  [Dodaj do koszyka]                 │
└─────────────────────────────────────┘
```

**Model:**
```java
// JEDEN ProductType - definicja produktu z konfigurowalnymi cechami
ProductType iphone15Pro = ProductType.builder(
    new UuidProductIdentifier(),
    ProductName.of("iPhone 15 Pro"),
    ProductDescription.of("Smartphone Apple z procesorem A17 Pro"),
    Unit.pieces(),
    ProductTrackingStrategy.INDIVIDUALLY_TRACKED
)
.withMandatoryFeature(storageFeature)  // 128GB, 256GB, 512GB, 1TB
.withMandatoryFeature(colorFeature)    // Space Black, Silver, Blue, Titanium
.build();

// JEDEN CatalogEntry - pozycja w katalogu
CatalogEntry entry = CatalogEntry.builder()
    .id(CatalogEntryId.of("IPHONE-15-PRO"))
    .displayName("iPhone 15 Pro")
    .description("Najnowszy iPhone z titanową obudową")
    .productType(iphone15Pro)
    .categories(Set.of("Smartfony", "Apple", "5G", "Premium"))
    .validity(Validity.always())
    .metadata(Map.of(
        "badge", "Nowość",
        "priority", "1"
    ))
    .build();
```

**Co się dzieje:**
1. Klient widzi **jedną** kartę produktu
2. Wybiera cechy: storage=256GB, color=Blue
3. System tworzy `ProductInstance` z konkretnymi wartościami features
4. Pricing Service kalkuluje cenę bazując na wybranych cechach

**Kiedy używać:**
- Cechy są konfigurowalne i nie wpływają drastycznie na cenę
- Chcesz pokazać klientowi "jeden produkt" z opcjami
- Warianty mają wspólny opis, zdjęcia, specyfikację

---

### Pattern 2: Osobne kafelki dla każdego wariantu

**Jak wygląda w UI:**
```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ [📱 128GB]       │  │ [📱 256GB]       │  │ [📱 512GB]       │
│ iPhone 15 Pro    │  │ iPhone 15 Pro    │  │ iPhone 15 Pro    │
│ 128GB            │  │ 256GB            │  │ 512GB            │
│ Space Black      │  │ Space Black      │  │ Space Black      │
│                  │  │                  │  │                  │
│ 5499 zł          │  │ 6299 zł          │  │ 7499 zł          │
│ [Kup teraz]      │  │ [Kup teraz]      │  │ [Kup teraz]      │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

**Model - Opcja A: Jeden ProductType, wiele features (rekombinowane)**

Tak jak Pattern 1, ale frontend renderuje jako osobne kafelki:

```java
// JEDEN ProductType z cechami
ProductType iphone15Pro = ProductType.builder(...)
    .withMandatoryFeature(storageFeature)
    .withMandatoryFeature(colorFeature)
    .build();

// JEDEN CatalogEntry
CatalogEntry entry = CatalogEntry.builder()
    .displayName("iPhone 15 Pro")
    .productType(iphone15Pro)
    .build();

// Frontend pobiera możliwe wartości features i renderuje jako kafelki
Set<String> storageOptions = iphone15Pro.featureTypes()
    .getFeatureType("storage")
    .constraint()
    .possibleValues(); // [128GB, 256GB, 512GB, 1TB]

// Dla każdej kombinacji (storage × color) = osobny kafelek w UI
```

**Model - Opcja B: Osobne ProductType dla głównych wariantów**

```java
// TRZY ProductTypes (osobne dla każdej pamięci)
ProductType iphone128 = ProductType.builder(
    new UuidProductIdentifier(),
    ProductName.of("iPhone 15 Pro 128GB"),
    ProductDescription.of("iPhone 15 Pro z 128GB pamięci"),
    Unit.pieces(),
    ProductTrackingStrategy.INDIVIDUALLY_TRACKED
)
.withMandatoryFeature(colorFeature)  // Tylko kolor do wyboru
.build();

ProductType iphone256 = ProductType.builder(
    new UuidProductIdentifier(),
    ProductName.of("iPhone 15 Pro 256GB"),
    ProductDescription.of("iPhone 15 Pro z 256GB pamięci"),
    Unit.pieces(),
    ProductTrackingStrategy.INDIVIDUALLY_TRACKED
)
.withMandatoryFeature(colorFeature)
.build();

ProductType iphone512 = ProductType.builder(
    new UuidProductIdentifier(),
    ProductName.of("iPhone 15 Pro 512GB"),
    ProductDescription.of("iPhone 15 Pro z 512GB pamięci"),
    Unit.pieces(),
    ProductTrackingStrategy.INDIVIDUALLY_TRACKED
)
.withMandatoryFeature(colorFeature)
.build();

// TRZY CatalogEntry (jeden per wariant pamięci)
CatalogEntry entry128 = CatalogEntry.builder()
    .displayName("iPhone 15 Pro 128GB")
    .productType(iphone128)  // 1:1
    .categories(Set.of("Smartfony", "Apple"))
    .metadata(Map.of("storage", "128GB", "basePrice", "5499"))
    .build();

CatalogEntry entry256 = CatalogEntry.builder()
    .displayName("iPhone 15 Pro 256GB")
    .productType(iphone256)  // 1:1
    .categories(Set.of("Smartfony", "Apple"))
    .metadata(Map.of("storage", "256GB", "basePrice", "6299"))
    .build();

CatalogEntry entry512 = CatalogEntry.builder()
    .displayName("iPhone 15 Pro 512GB")
    .productType(iphone512)  // 1:1
    .categories(Set.of("Smartfony", "Apple"))
    .metadata(Map.of("storage", "512GB", "basePrice", "7499"))
    .build();
```

**Kiedy używać:**
- Warianty różnią się znacząco ceną lub dostępnością
- Każdy wariant ma własną stronę produktu / SEO
- Chcesz osobno śledzić sprzedaż/inventory dla każdego wariantu
- Marketing chce osobno promować różne warianty

**Zalety Opcji B:**
- Prosta relacja 1:1
- Każdy wariant ma własną cenę, dostępność, promocje
- Łatwo zarządzać stanem (256GB wyprzedane? Usuwamy z katalogu)
- Każdy wariant może mieć własny opis SEO

**Wady Opcji B:**
- Więcej wpisów w katalogu (3 pamięci × 4 kolory = 12 wpisów jeśli każda kombinacja ma osobny ProductType)
- Zmiana wspólnych danych (np. spec technicznych) wymaga aktualizacji wielu definicji

---

## Nasza decyzja: 1 CatalogEntry = 1 ProductType

Model, który zaimplementowaliśmy, zakłada **relację 1:1**.

**Dlaczego:**
1. **Prostota** - łatwiej zrozumieć i utrzymać
2. **Elastyczność przez Features** - różnice w wariantach wyrażamy przez ProductFeatureType, nie przez mnożenie ProductTypes
3. **Clear ownership** - jeden wpis w katalogu = jedna definicja produktu

**Kiedy tworzyć osobne ProductType:**
- Produkty faktycznie różnią się biznesowo (różne SKU, różne dostawcy, różne koszty)
- Potrzebujesz osobnej kontroli nad lifecycle (jeden wariant wycofany, inne aktywne)
- Marketing wymaga osobnych stron/kampanii dla wariantów

**Kiedy używać Features w ramach jednego ProductType:**
- Różnice są konfigurowalne (rozmiar, kolor, wykończenie)
- Warianty mają wspólną logikę biznesową
- Cechy nie wpływają na tracking/fulfillment (nadal ten sam typ produktu)

---

## Przykłady z różnych branż

### Retail - Koszulka

**Pattern 1 (konfigurator):**
```java
ProductType tshirt = ProductType.builder(...)
    .withMandatoryFeature(sizeFeature)    // S, M, L, XL
    .withMandatoryFeature(colorFeature)   // Red, Blue, Black
    .build();

CatalogEntry entry = CatalogEntry.builder()
    .displayName("Koszulka Classic")
    .productType(tshirt)
    .build();

// Klient wybiera rozmiar + kolor w jednym miejscu
```

**Pattern 2 (osobne kafelki):**
```java
ProductType tshirtS = ...;   // Tylko kolor do wyboru
ProductType tshirtM = ...;
ProductType tshirtL = ...;

CatalogEntry entryS = ...;   // "Koszulka Classic S"
CatalogEntry entryM = ...;   // "Koszulka Classic M"
CatalogEntry entryL = ...;   // "Koszulka Classic L"

// Każdy rozmiar jako osobny kafelek (np. dla odzieży dziecięcej gdzie rozmiar = wiek)
```

### Bankowość - Lokaty

```java
// Lokata z elastycznym okresem i kwotą
ProductType deposit = ProductType.builder(...)
    .withMandatoryFeature(termFeature)       // 1-36 miesięcy
    .withMandatoryFeature(minAmountFeature)  // 1000-1000000 PLN
    .build();

CatalogEntry depositOffer = CatalogEntry.builder()
    .displayName("Lokata Oszczędnościowa")
    .productType(deposit)
    .validity(Validity.from(LocalDate.of(2024, 1, 1)))
    .metadata(Map.of(
        "interestRate", "7.5%",
        "campaign", "Q1-2024"
    ))
    .build();

// Klient sam definiuje okres i kwotę (w ramach constraintów)
```

### Medycyna - Wizyty

```java
// Wizyta specjalistyczna
ProductType consultation = ProductType.builder(...)
    .withMandatoryFeature(specializationFeature)  // Kardiologia, Dermatologia
    .withMandatoryFeature(durationFeature)        // 15min, 30min, 60min
    .build();

CatalogEntry consultationOffer = CatalogEntry.builder()
    .displayName("Konsultacja specjalistyczna")
    .productType(consultation)
    .categories(Set.of("Konsultacje", "Specjaliści"))
    .build();

// Lub osobne entries dla każdej specjalizacji (jeśli różne ceny/lekarze)
```

---

## Podsumowanie

| Aspekt | Pattern 1 (konfigurator) | Pattern 2 (kafelki) |
|--------|--------------------------|---------------------|
| **ProductTypes** | 1 z features | 1 z features (opcja A) lub wiele (opcja B) |
| **CatalogEntries** | 1 | 1 (opcja A) lub wiele (opcja B) |
| **UI** | Jeden widok z selectorami | Wiele kafelków |
| **Pricing** | Kalkulowany dynamicznie | Per wariant (opcja B) |
| **Inventory** | Tracking per konkretna kombinacja features | Tracking per ProductType/Entry |
| **Kiedy** | Cechy konfigurowalne, wspólna logika | Warianty różnią się biznesowo |

**Nasza implementacja wspiera oba pattern'y:**
- Pattern 1: 1 CatalogEntry z ProductType z features
- Pattern 2A: 1 CatalogEntry, frontend rekombinuje features
- Pattern 2B: Wiele CatalogEntry, każdy z własnym ProductType

Wybór zależy od biznesu i wymagań UX.
