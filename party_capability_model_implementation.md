# Party Capability Model - Implementacja

## Koncepcje Kluczowe

### 1. Capability
Reprezentuje zdolność Party do wykonania określonej czynności. Przykłady:
- "Maria Nowak potrafi wykonać USG w Medicover Warszawa (max 15 pacjentów/dzień, pon-pt 8-16)"
- "Jan Kowalski instaluje instalacje elektryczne w Krakowie (poziom 9/10, SEP G1)"
- "DevPro Software rozwija aplikacje webowe (15 równoczesnych projektów, 75 osób)"

### 2. CapabilityType
Katalog typów capabilities (wzorzec Type-Instance, analogia do ProductType/PartyRoleType):
- "Medical Imaging"
- "Electrical Installation"
- "Programming"
- "Goods Delivery"
- "Software Development"
- "Beauty Services"

### 3. Operating Scopes
Zakresy operacyjne definiujące **granice** capability:
- **WHERE?** → LocationScope (lokalizacja, zasięg)
- **WHAT?** → ProductScope, ResourceScope (produkty, zasoby)
- **HOW MUCH?** → QuantityScope (ilości, limity)
- **HOW WELL?** → SkillLevelScope (poziom umiejętności)
- **WHEN?** → TemporalScope (dostępność czasowa)
- **WHICH PROTOCOL?** → ProtocolScope (procedury, protokoły)

---

## Model Domenowy - Implementacja Java

### CapabilityId

```java
package com.softwarearchetypes.party;

import java.util.UUID;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record CapabilityId(UUID value) {
    public CapabilityId {
        checkArgument(value != null, "Capability Id value cannot be null");
    }

    public String asString() {
        return value.toString();
    }

    public static CapabilityId of(UUID value) {
        return new CapabilityId(value);
    }

    public static CapabilityId random() {
        return of(UUID.randomUUID());
    }
}
```

### CapabilityType

```java
package com.softwarearchetypes.party;

import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record CapabilityType(
    CapabilityTypeId id,
    String name,
    String description,
    Set<Class<? extends OperatingScope>> requiredScopeTypes
) {

    public CapabilityType {
        checkArgument(id != null, "CapabilityType id cannot be null");
        checkArgument(isNotBlank(name), "CapabilityType name cannot be blank");
        checkArgument(requiredScopeTypes != null, "Required scope types cannot be null");
    }

    public static CapabilityType define(
        String name,
        String description,
        Class<? extends OperatingScope>... requiredScopeTypes
    ) {
        return new CapabilityType(
            CapabilityTypeId.random(),
            name,
            description,
            Set.of(requiredScopeTypes)
        );
    }

    public boolean requiresScope(Class<? extends OperatingScope> scopeType) {
        return requiredScopeTypes.contains(scopeType);
    }

    public boolean validateScopes(Set<OperatingScope> scopes) {
        // Sprawdź czy wszystkie wymagane scope types są obecne
        Set<Class<? extends OperatingScope>> providedTypes = scopes.stream()
            .map(OperatingScope::getClass)
            .collect(Collectors.toSet());

        return providedTypes.containsAll(requiredScopeTypes);
    }
}
```

### Capability

```java
package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record Capability(
    CapabilityId id,
    PartyId partyId,
    CapabilityType capabilityType,
    Set<OperatingScope> operatingScopes,
    Validity validity
) {

    public Capability {
        checkArgument(id != null, "Capability id cannot be null");
        checkArgument(partyId != null, "Party id cannot be null");
        checkArgument(capabilityType != null, "Capability type cannot be null");
        checkArgument(operatingScopes != null, "Operating scopes cannot be null");
        checkArgument(validity != null, "Validity cannot be null");

        // Walidacja: czy wszystkie wymagane scopes są obecne
        if (!capabilityType.validateScopes(operatingScopes)) {
            throw new IllegalArgumentException(
                "Capability must have all required operating scopes for type: " + capabilityType.name()
            );
        }
    }

    public static Capability establish(
        PartyId partyId,
        CapabilityType capabilityType,
        Validity validity,
        OperatingScope... scopes
    ) {
        return new Capability(
            CapabilityId.random(),
            partyId,
            capabilityType,
            new HashSet<>(Set.of(scopes)),
            validity
        );
    }

    public boolean isActive() {
        return validity.isActiveAt(LocalDate.now());
    }

    public boolean hasScope(Class<? extends OperatingScope> scopeType) {
        return operatingScopes.stream()
            .anyMatch(scope -> scopeType.isInstance(scope));
    }

    @SuppressWarnings("unchecked")
    public <T extends OperatingScope> Optional<T> getScope(Class<T> scopeType) {
        return operatingScopes.stream()
            .filter(scopeType::isInstance)
            .map(scope -> (T) scope)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    public <T extends OperatingScope> Set<T> getScopes(Class<T> scopeType) {
        return operatingScopes.stream()
            .filter(scopeType::isInstance)
            .map(scope -> (T) scope)
            .collect(Collectors.toSet());
    }

    public Capability addScope(OperatingScope scope) {
        Set<OperatingScope> newScopes = new HashSet<>(operatingScopes);
        newScopes.add(scope);
        return new Capability(id, partyId, capabilityType, newScopes, validity);
    }

    public Capability removeScope(OperatingScope scope) {
        Set<OperatingScope> newScopes = new HashSet<>(operatingScopes);
        newScopes.remove(scope);
        return new Capability(id, partyId, capabilityType, newScopes, validity);
    }
}
```

---

## Operating Scopes - Implementacja

### OperatingScope (abstrakcja)

```java
package com.softwarearchetypes.party;

public interface OperatingScope {

    /**
     * Walidacja poprawności scope
     */
    boolean validate();

    /**
     * Typ scope (do identyfikacji)
     */
    String scopeType();
}
```

### LocationScope

```java
package com.softwarearchetypes.party;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record LocationScope(
    String location,
    Distance radius  // może być null = unlimited
) implements OperatingScope {

    public LocationScope {
        checkArgument(isNotBlank(location), "Location cannot be blank");
    }

    public static LocationScope of(String location) {
        return new LocationScope(location, null);
    }

    public static LocationScope withRadius(String location, Distance radius) {
        return new LocationScope(location, radius);
    }

    @Override
    public boolean validate() {
        return isNotBlank(location);
    }

    @Override
    public String scopeType() {
        return "LOCATION";
    }

    public boolean coversLocation(String checkLocation) {
        // Uproszczona logika - w produkcji użyć geolokalizacji
        return location.equalsIgnoreCase(checkLocation);
    }

    public boolean isUnlimited() {
        return radius == null;
    }
}

// Distance - value object
public record Distance(double value, DistanceUnit unit) {
    public static Distance km(double value) {
        return new Distance(value, DistanceUnit.KILOMETERS);
    }
}

public enum DistanceUnit {
    KILOMETERS, MILES
}
```

### ProductScope

```java
package com.softwarearchetypes.party;

import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record ProductScope(
    Set<String> productTypes,      // konkretne typy produktów
    Set<String> productCategories  // kategorie produktów
) implements OperatingScope {

    public ProductScope {
        checkArgument(productTypes != null, "Product types cannot be null");
        checkArgument(productCategories != null, "Product categories cannot be null");
        checkArgument(!productTypes.isEmpty() || !productCategories.isEmpty(),
            "At least one product type or category must be specified");
    }

    public static ProductScope forTypes(String... types) {
        return new ProductScope(Set.of(types), Set.of());
    }

    public static ProductScope forCategories(String... categories) {
        return new ProductScope(Set.of(), Set.of(categories));
    }

    public static ProductScope of(Set<String> types, Set<String> categories) {
        return new ProductScope(types, categories);
    }

    @Override
    public boolean validate() {
        return !productTypes.isEmpty() || !productCategories.isEmpty();
    }

    @Override
    public String scopeType() {
        return "PRODUCT";
    }

    public boolean coversProduct(String productType) {
        return productTypes.contains(productType);
    }

    public boolean coversCategory(String category) {
        return productCategories.contains(category);
    }
}
```

### SkillLevelScope

```java
package com.softwarearchetypes.party;

import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record SkillLevelScope(
    String skillName,
    int level,  // 1-10
    Set<String> certifications
) implements OperatingScope {

    public SkillLevelScope {
        checkArgument(isNotBlank(skillName), "Skill name cannot be blank");
        checkArgument(level >= 1 && level <= 10, "Level must be between 1 and 10");
        checkArgument(certifications != null, "Certifications cannot be null");
    }

    public static SkillLevelScope of(String skillName, int level) {
        return new SkillLevelScope(skillName, level, Set.of());
    }

    public static SkillLevelScope withCertifications(String skillName, int level, String... certs) {
        return new SkillLevelScope(skillName, level, Set.of(certs));
    }

    @Override
    public boolean validate() {
        return isNotBlank(skillName) && level >= 1 && level <= 10;
    }

    @Override
    public String scopeType() {
        return "SKILL_LEVEL";
    }

    public boolean meetsMinimumLevel(int requiredLevel) {
        return level >= requiredLevel;
    }

    public boolean hasCertification(String certification) {
        return certifications.contains(certification);
    }

    public boolean isExpert() {
        return level >= 8;
    }
}
```

### QuantityScope

```java
package com.softwarearchetypes.party;

import java.time.Period;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record QuantityScope(
    Quantity quantityPerPeriod,
    Period period
) implements OperatingScope {

    public QuantityScope {
        checkArgument(quantityPerPeriod != null, "Quantity cannot be null");
        checkArgument(period != null, "Period cannot be null");
    }

    public static QuantityScope perYear(int quantity, String unit) {
        return new QuantityScope(
            new Quantity(quantity, unit),
            Period.ofYears(1)
        );
    }

    public static QuantityScope perMonth(int quantity, String unit) {
        return new QuantityScope(
            new Quantity(quantity, unit),
            Period.ofMonths(1)
        );
    }

    public static QuantityScope perDay(int quantity, String unit) {
        return new QuantityScope(
            new Quantity(quantity, unit),
            Period.ofDays(1)
        );
    }

    @Override
    public boolean validate() {
        return quantityPerPeriod != null && period != null;
    }

    @Override
    public String scopeType() {
        return "QUANTITY";
    }

    public boolean canHandle(int requestedQuantity, Period requestedPeriod) {
        // Normalizacja do dni dla porównania
        long requestedDays = requestedPeriod.getDays() + requestedPeriod.getMonths() * 30L + requestedPeriod.getYears() * 365L;
        long scopeDays = period.getDays() + period.getMonths() * 30L + period.getYears() * 365L;

        double requestedPerDay = (double) requestedQuantity / requestedDays;
        double scopePerDay = (double) quantityPerPeriod.value() / scopeDays;

        return requestedPerDay <= scopePerDay;
    }
}

// Quantity - value object
public record Quantity(int value, String unit) {
    public Quantity {
        checkArgument(value >= 0, "Quantity value must be non-negative");
        checkArgument(isNotBlank(unit), "Quantity unit cannot be blank");
    }
}
```

### ProtocolScope

```java
package com.softwarearchetypes.party;

import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record ProtocolScope(
    String protocolName,
    Set<String> procedures
) implements OperatingScope {

    public ProtocolScope {
        checkArgument(isNotBlank(protocolName), "Protocol name cannot be blank");
        checkArgument(procedures != null && !procedures.isEmpty(), "Procedures cannot be empty");
    }

    public static ProtocolScope of(String protocolName, String... procedures) {
        return new ProtocolScope(protocolName, Set.of(procedures));
    }

    @Override
    public boolean validate() {
        return isNotBlank(protocolName) && !procedures.isEmpty();
    }

    @Override
    public String scopeType() {
        return "PROTOCOL";
    }

    public boolean supportsProtocol(String protocol) {
        return protocolName.equalsIgnoreCase(protocol);
    }

    public boolean supportsProcedure(String procedure) {
        return procedures.contains(procedure);
    }
}
```

### TemporalScope

```java
package com.softwarearchetypes.party;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record TemporalScope(
    LocalTime availableFrom,
    LocalTime availableTo,
    Set<DayOfWeek> daysOfWeek
) implements OperatingScope {

    public TemporalScope {
        checkArgument(availableFrom != null, "Available from cannot be null");
        checkArgument(availableTo != null, "Available to cannot be null");
        checkArgument(availableFrom.isBefore(availableTo), "From must be before To");
        checkArgument(daysOfWeek != null && !daysOfWeek.isEmpty(), "Days of week cannot be empty");
    }

    public static TemporalScope businessHours() {
        return new TemporalScope(
            LocalTime.of(8, 0),
            LocalTime.of(17, 0),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                   DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        );
    }

    public static TemporalScope allDay(DayOfWeek... days) {
        return new TemporalScope(
            LocalTime.of(0, 0),
            LocalTime.of(23, 59),
            Set.of(days)
        );
    }

    @Override
    public boolean validate() {
        return availableFrom != null && availableTo != null && !daysOfWeek.isEmpty();
    }

    @Override
    public String scopeType() {
        return "TEMPORAL";
    }

    public boolean isAvailableAt(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        DayOfWeek day = dateTime.getDayOfWeek();

        return daysOfWeek.contains(day) &&
               !time.isBefore(availableFrom) &&
               !time.isAfter(availableTo);
    }
}
```

### ResourceScope

```java
package com.softwarearchetypes.party;

import java.util.Set;
import static com.softwarearchetypes.common.Preconditions.checkArgument;

public record ResourceScope(
    Set<String> resourceTypes,
    Quantity maxCapacity
) implements OperatingScope {

    public ResourceScope {
        checkArgument(resourceTypes != null && !resourceTypes.isEmpty(), "Resource types cannot be empty");
        checkArgument(maxCapacity != null, "Max capacity cannot be null");
    }

    public static ResourceScope of(Quantity maxCapacity, String... types) {
        return new ResourceScope(Set.of(types), maxCapacity);
    }

    @Override
    public boolean validate() {
        return !resourceTypes.isEmpty() && maxCapacity != null;
    }

    @Override
    public String scopeType() {
        return "RESOURCE";
    }

    public boolean canHandleResource(String resourceType, Quantity quantity) {
        return resourceTypes.contains(resourceType) &&
               quantity.value() <= maxCapacity.value();
    }

    public boolean supportsResourceType(String resourceType) {
        return resourceTypes.contains(resourceType);
    }
}
```

---

## Przykłady Użycia

### Przykład 1: USG Technician

```java
// Definicja typu capability
CapabilityType medicalImaging = CapabilityType.define(
    "Medical Imaging",
    "Capability to perform medical imaging procedures",
    ProtocolScope.class,
    QuantityScope.class,
    LocationScope.class,
    TemporalScope.class
);

// Przypisanie capability do Maria Nowak
Capability mariaNowakUSG = Capability.establish(
    mariaNowakId,
    medicalImaging,
    Validity.from(LocalDate.of(2024, 1, 1)).to(LocalDate.of(2026, 12, 31)),
    ProtocolScope.of("Ultrasound Imaging", "Abdominal", "Cardiac", "Prenatal", "Thyroid"),
    QuantityScope.perDay(15, "patients"),
    LocationScope.of("Medicover Warszawa Mokotów"),
    TemporalScope.businessHours()  // Mon-Fri, 8:00-17:00
);

// Sprawdzenie możliwości
boolean canPerformOnWednesday = mariaNowakUSG
    .getScope(TemporalScope.class)
    .map(scope -> scope.isAvailableAt(LocalDateTime.of(2024, 6, 12, 10, 0))) // Wednesday
    .orElse(false);
// → true

boolean canHandle20Patients = mariaNowakUSG
    .getScope(QuantityScope.class)
    .map(scope -> scope.canHandle(20, Period.ofDays(1)))
    .orElse(false);
// → false (limit is 15/day)
```

### Przykład 2: Java Developer

```java
// Definicja typu capability
CapabilityType programming = CapabilityType.define(
    "Programming",
    "Software development capability",
    SkillLevelScope.class
);

// Przypisanie capability do Anna
Capability annaJava = Capability.establish(
    annaId,
    programming,
    Validity.forever(),
    SkillLevelScope.withCertifications("Java", 8, "Oracle Certified Java Programmer"),
    SkillLevelScope.withCertifications("Spring Framework", 7, "Spring Professional"),
    LocationScope.of("Remote (Poland)")
);

// Sprawdzenie poziomu umiejętności
boolean hasJavaExpertise = annaJava
    .getScope(SkillLevelScope.class)
    .map(SkillLevelScope::isExpert)
    .orElse(false);
// → true (level 8 >= 8)

Set<SkillLevelScope> allSkills = annaJava.getScopes(SkillLevelScope.class);
// → [Java: 8/10, Spring Framework: 7/10]
```

### Przykład 3: Electrician

```java
// Definicja typu capability
CapabilityType electricalInstallation = CapabilityType.define(
    "Electrical Installation",
    "Electrical installation and repair capability",
    SkillLevelScope.class,
    ProtocolScope.class,
    LocationScope.class,
    TemporalScope.class
);

// Przypisanie capability do Jan Kowalski
Capability janKowalskiElectric = Capability.establish(
    janKowalskiId,
    electricalInstallation,
    Validity.forever(),
    SkillLevelScope.withCertifications("Electrical Installation", 9, "SEP G1", "SEP D"),
    ProtocolScope.of("Building Installations", "Residential", "Commercial", "Industrial"),
    LocationScope.withRadius("Kraków", Distance.km(30)),
    TemporalScope.of(LocalTime.of(7, 0), LocalTime.of(19, 0), DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
);

// Sprawdzenie czy może wykonać instalację przemysłową
boolean canDoIndustrial = janKowalskiElectric
    .getScope(ProtocolScope.class)
    .map(scope -> scope.supportsProcedure("Industrial"))
    .orElse(false);
// → true

// Sprawdzenie czy pracuje w niedzielę
boolean availableOnSunday = janKowalskiElectric
    .getScope(TemporalScope.class)
    .map(scope -> scope.isAvailableAt(LocalDateTime.of(2024, 6, 9, 10, 0))) // Sunday
    .orElse(false);
// → false (Mon-Sat only)
```

### Przykład 4: Delivery Driver

```java
// Definicja typu capability
CapabilityType goodsDelivery = CapabilityType.define(
    "Goods Delivery",
    "Logistics delivery capability",
    LocationScope.class,
    ProductScope.class,
    QuantityScope.class,
    ResourceScope.class,
    TemporalScope.class
);

// Przypisanie capability do Piotr Wiśniewski
Capability piotrDelivery = Capability.establish(
    piotrWisniewskiId,
    goodsDelivery,
    Validity.from(LocalDate.of(2024, 1, 1)).to(LocalDate.of(2025, 12, 31)),
    LocationScope.withRadius("Warszawa i okolice", Distance.km(50)),
    ProductScope.of(
        Set.of(),
        Set.of("Electronics", "Furniture", "Food")
    ),
    QuantityScope.perDay(25, "deliveries"),
    ResourceScope.of(
        new Quantity(15, "m³"),
        "Van 3.5t"
    ),
    TemporalScope.allDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
);

// Sprawdzenie możliwości dostawy
boolean canDeliverElectronics = piotrDelivery
    .getScope(ProductScope.class)
    .map(scope -> scope.coversCategory("Electronics"))
    .orElse(false);
// → true

boolean canDeliver30Packages = piotrDelivery
    .getScope(QuantityScope.class)
    .map(scope -> scope.canHandle(30, Period.ofDays(1)))
    .orElse(false);
// → false (limit is 25/day)
```

### Przykład 5: Software House (Organization)

```java
// Definicja typu capability
CapabilityType softwareDevelopment = CapabilityType.define(
    "Software Development",
    "Custom software development capability",
    SkillLevelScope.class,
    ProductScope.class,
    QuantityScope.class,
    ResourceScope.class
);

// Capability z wieloma SkillLevelScopes (różne technologie)
Capability devProCapability = Capability.establish(
    devProSoftwareId,
    softwareDevelopment,
    Validity.forever(),
    SkillLevelScope.withCertifications("Java/Spring Boot", 9, "Oracle Partner", "Spring Certified"),
    SkillLevelScope.of("React/TypeScript", 8),
    ProductScope.forCategories("Web Applications", "Mobile Apps", "API Development", "Cloud Migration"),
    QuantityScope.of(15, "concurrent projects", Period.ofDays(1)),
    ResourceScope.of(
        new Quantity(75, "people"),
        "Senior Developers: 25",
        "Mid Developers: 40",
        "QA Engineers: 10"
    )
);

// Wyszukanie wszystkich umiejętności
Set<SkillLevelScope> techStack = devProCapability.getScopes(SkillLevelScope.class);
// → [Java/Spring Boot: 9/10, React/TypeScript: 8/10]

// Sprawdzenie czy może wziąć nowy projekt
boolean canTakeNewProject = devProCapability
    .getScope(QuantityScope.class)
    .map(scope -> scope.canHandle(20, Period.ofDays(1)))
    .orElse(false);
// → false (limit is 15 concurrent projects)
```

---

## Integracja z PartyRole - Matching Requirements

### Kluczowa różnica: Capability vs RoleRequirements

**WAŻNE**: Capability i RoleRequirements przypinamy do różnych encji:

```
Capability        → przypinamy do PARTY (konkretna osoba/organizacja)
RoleRequirements  → przypinamy do PARTYROLETYPE (definicja roli)
```

**Dlaczego PartyRoleType jest potrzebne?**

Wymagania dla roli są **wspólne dla wszystkich** osób pełniących daną rolę. Zamiast duplikować te wymagania przy każdym PartyRole, definiujemy je raz w PartyRoleType (wzorzec Type-Instance).

### Model

```java
// PartyRoleType - KATALOG ról (z wymaganiami)
public record PartyRoleType(
    PartyRoleTypeId id,
    String name,
    String description,
    Set<PartyRoleConstraint> constraints,
    RoleRequirements requirements  // ✓ Wymagania zdefiniowane RAZ
) {
}

// PartyRole - INSTANCJA (konkretne przypisanie)
public record PartyRole(
    PartyRoleId id,
    PartyId partyId,
    PartyRoleType roleType,  // ✓ Referencja do typu (z wymaganiami)
    Validity validity
) {
}

// Capability - umiejętności PARTY
public record Capability(
    CapabilityId id,
    PartyId partyId,  // ✓ Bezpośrednio do Party
    CapabilityType capabilityType,
    Set<OperatingScope> operatingScopes,
    Validity validity
) {
}
```

### RoleRequirements - Implementacja

```java
// RoleRequirements przypinane do PartyRoleType
public record RoleRequirements(
    Set<CapabilityRequirement> requiredCapabilities
) {

    public static RoleRequirements none() {
        return new RoleRequirements(Set.of());
    }

    public static RoleRequirements require(CapabilityRequirement... requirements) {
        return new RoleRequirements(Set.of(requirements));
    }

    public boolean areSatisfiedBy(Set<Capability> partyCapabilities) {
        return requiredCapabilities.stream()
            .allMatch(req -> req.isSatisfiedBy(partyCapabilities));
    }

    public boolean hasRequirements() {
        return !requiredCapabilities.isEmpty();
    }
}

// Pojedyncze wymaganie capability
public record CapabilityRequirement(
    CapabilityTypeId capabilityTypeId,
    Set<ScopeRequirement> scopeRequirements
) {

    public static CapabilityRequirement of(
        CapabilityTypeId typeId,
        ScopeRequirement... scopeReqs
    ) {
        return new CapabilityRequirement(typeId, Set.of(scopeReqs));
    }

    public boolean isSatisfiedBy(Set<Capability> partyCapabilities) {
        return partyCapabilities.stream()
            .filter(cap -> cap.capabilityType().id().equals(capabilityTypeId))
            .filter(Capability::isActive)  // tylko aktywne capabilities
            .anyMatch(cap -> scopeRequirements.stream()
                .allMatch(req -> req.isSatisfiedBy(cap)));
    }
}

// Wymagania dotyczące scopes
public interface ScopeRequirement {
    boolean isSatisfiedBy(Capability capability);
}

public record MinimumSkillLevelRequirement(int minimumLevel) implements ScopeRequirement {

    @Override
    public boolean isSatisfiedBy(Capability capability) {
        return capability.getScope(SkillLevelScope.class)
            .map(scope -> scope.level() >= minimumLevel)
            .orElse(false);
    }
}

public record MinimumQuantityRequirement(int quantity, Period period) implements ScopeRequirement {

    @Override
    public boolean isSatisfiedBy(Capability capability) {
        return capability.getScope(QuantityScope.class)
            .map(scope -> scope.canHandle(quantity, period))
            .orElse(false);
    }
}

public record RequiredProtocolRequirement(String protocolName) implements ScopeRequirement {

    @Override
    public boolean isSatisfiedBy(Capability capability) {
        return capability.getScope(ProtocolScope.class)
            .map(scope -> scope.supportsProtocol(protocolName))
            .orElse(false);
    }
}

public record RequiredCertificationRequirement(String certification) implements ScopeRequirement {

    @Override
    public boolean isSatisfiedBy(Capability capability) {
        return capability.getScopes(SkillLevelScope.class).stream()
            .anyMatch(scope -> scope.hasCertification(certification));
    }
}
```

---

## Przykład Biznesowy: Senior Electrician

### Scenariusz

Firma budowlana potrzebuje definicji roli **"Senior Electrician"**. Wszyscy seniorzy muszą spełniać te same wymagania, więc definiujemy je raz w **PartyRoleType**.

### Krok 1: Definicja PartyRoleType z wymaganiami

```java
// Wymagania dla roli Senior Electrician (zdefiniowane RAZ)
RoleRequirements seniorElectricianReqs = RoleRequirements.require(
    CapabilityRequirement.of(
        electricalInstallationTypeId,
        new MinimumSkillLevelRequirement(8),  // min poziom 8/10
        new RequiredCertificationRequirement("SEP G1"),  // musi mieć SEP G1
        new RequiredProtocolRequirement("Industrial")  // musi umieć instalacje przemysłowe
    )
);

// Definicja PartyRoleType
PartyRoleType seniorElectricianType = PartyRoleType.define(
    "Senior Electrician",
    "Experienced electrician capable of complex industrial installations",
    seniorElectricianReqs,  // ✓ Wymagania przypięte do typu roli
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(25)
);

// ✓ Wymagania przechowywane RAZ w PartyRoleType
// ✓ Wszystkie osoby z rolą Senior Electrician mają te same wymagania
```

### Krok 2: Party z Capabilities

```java
// Jan Kowalski ma capabilities (przypięte do PARTY, nie do roli!)
Capability janElectric = Capability.establish(
    janKowalskiId,  // ✓ Bezpośrednio do Party
    electricalInstallationType,
    Validity.forever(),
    SkillLevelScope.withCertifications("Electrical Installation", 9, "SEP G1", "SEP D"),
    ProtocolScope.of("Building Installations", "Residential", "Commercial", "Industrial"),
    LocationScope.withRadius("Kraków", Distance.km(30))
);

// Piotr Nowak ma capabilities (też przypięte do PARTY)
Capability piotrElectric = Capability.establish(
    piotrNowakId,  // ✓ Bezpośrednio do Party
    electricalInstallationType,
    Validity.forever(),
    SkillLevelScope.withCertifications("Electrical Installation", 6, "SEP D"),  // tylko 6/10, brak SEP G1
    ProtocolScope.of("Building Installations", "Residential", "Commercial"),  // brak Industrial
    LocationScope.withRadius("Warszawa", Distance.km(20))
);
```

### Krok 3: Sprawdzenie i przypisanie roli

```java
// Pobranie wszystkich capabilities Jana
Set<Capability> janCapabilities = capabilityRepository.findByPartyId(janKowalskiId);

// Sprawdzenie czy Jan spełnia wymagania dla Senior Electrician
boolean janCanBeAssigned = seniorElectricianType
    .requirements()
    .areSatisfiedBy(janCapabilities);
// → TRUE (ma level 9 >= 8, SEP G1, umie Industrial)

// Przypisanie roli
if (janCanBeAssigned) {
    PartyRole janAsSeniorElectrician = PartyRole.assign(
        janKowalskiId,
        seniorElectricianType,  // ✓ Referencja do typu (z wymaganiami)
        Validity.forever(),
        janParty
    );
    partyRoleRepository.save(janAsSeniorElectrician);
}

// Pobranie wszystkich capabilities Piotra
Set<Capability> piotrCapabilities = capabilityRepository.findByPartyId(piotrNowakId);

// Sprawdzenie czy Piotr spełnia wymagania dla Senior Electrician
boolean piotrCanBeAssigned = seniorElectricianType
    .requirements()
    .areSatisfiedBy(piotrCapabilities);
// → FALSE (level tylko 6 < 8, brak SEP G1, brak Industrial)

// ✗ Piotr NIE może dostać roli Senior Electrician
if (!piotrCanBeAssigned) {
    throw new RoleAssignmentFailedException(
        "Party does not satisfy requirements for role: " + seniorElectricianType.name()
    );
}
```

### Krok 4: Ewolucja - Piotr zdobywa certyfikat

```java
// Rok później: Piotr zdobywa SEP G1 i podnosi poziom
Capability piotrElectricUpdated = Capability.establish(
    piotrNowakId,
    electricalInstallationType,
    Validity.forever(),
    SkillLevelScope.withCertifications("Electrical Installation", 8, "SEP G1", "SEP D"),  // ✓ 8/10, SEP G1
    ProtocolScope.of("Building Installations", "Residential", "Commercial", "Industrial"),  // ✓ Industrial
    LocationScope.withRadius("Warszawa", Distance.km(20))
);

// Teraz Piotr spełnia wymagania
Set<Capability> piotrUpdatedCapabilities = capabilityRepository.findByPartyId(piotrNowakId);
boolean piotrNowCanBeAssigned = seniorElectricianType
    .requirements()
    .areSatisfiedBy(piotrUpdatedCapabilities);
// → TRUE

// ✓ Piotr może dostać rolę Senior Electrician
PartyRole piotrAsSeniorElectrician = PartyRole.assign(
    piotrNowakId,
    seniorElectricianType,
    Validity.from(LocalDate.now()),
    piotrParty
);
```

### Zalety tego podejścia

```
✓ Wymagania zdefiniowane RAZ w PartyRoleType
  - Nie duplikujemy wymagań przy każdym PartyRole
  - Zmiana wymagań = jedna zmiana w PartyRoleType

✓ Capabilities przypięte do Party (nie do roli)
  - Party ma capabilities niezależnie od ról
  - Te same capabilities mogą spełniać wymagania wielu ról

✓ Automatyczne matchowanie
  - System sprawdza czy Party ma odpowiednie capabilities
  - Rules-based validation przed przypisaniem roli

✓ Ewolucja capabilities
  - Party może zdobywać nowe capabilities
  - System automatycznie ocenia czy nowe capabilities
    spełniają wymagania dostępnych ról
```

### Przykład: 10 Senior Electricians

```java
// Wymagania zdefiniowane RAZ w PartyRoleType
PartyRoleType seniorElectricianType = PartyRoleType.define(
    "Senior Electrician",
    "...",
    seniorElectricianReqs  // ✓ Jedna definicja wymagań
);

// 10 osób z rolą Senior Electrician
PartyRole jan = PartyRole.assign(janId, seniorElectricianType, ...);
PartyRole anna = PartyRole.assign(annaId, seniorElectricianType, ...);
PartyRole tomasz = PartyRole.assign(tomaszId, seniorElectricianType, ...);
// ... 7 more

// ✓ Wymagania przechowywane RAZ, nie 10 razy
// ✓ Zmiana wymagań = jedna zmiana w seniorElectricianType
// ✓ Wszystkie 10 osób automatycznie ma nowe wymagania
```

---

## Podsumowanie: Capability vs RoleRequirements

| Aspekt | Capability | RoleRequirements |
|--------|-----------|------------------|
| **Przypięte do** | Party | PartyRoleType |
| **Znaczenie** | Co Party **POTRAFI** | Co rola **WYMAGA** |
| **Przykład** | "Jan ma skill level 9" | "Senior wymaga min level 8" |
| **Ilość** | Wiele per Party | Jeden zestaw per PartyRoleType |
| **Zmiana** | Ewoluuje z Party | Rzadko się zmienia |
| **Walidacja** | Sprawdzane przy assign | Definiowane w type |

---

## Podsumowanie

### Zalety modelu Capability + Operating Scopes:

1. **Elastyczność** - różne typy scopes dla różnych capabilities
2. **Reużywalność** - OperatingScope mogą być współdzielone między capabilities
3. **Rozszerzalność** - łatwo dodać nowe typy scopes (np. CostScope, QualityScope)
4. **Rules-based matching** - automatyczne dopasowanie capabilities do wymagań ról
5. **Rich queries** - "kto potrafi X w lokalizacji Y z poziomem Z?"
6. **Type-Instance pattern** - CapabilityType jako katalog (jak ProductType, PartyRoleType)

### Możliwe rozszerzenia:

- **CapabilityHistory** - historia zmian capabilities (poziomy umiejętności rosną)
- **CapabilityExpiration** - wygasanie capabilities (certyfikaty tracą ważność)
- **CapabilityCost** - koszt wykorzystania capability (stawka godzinowa)
- **CapabilityQuality** - metryki jakości (np. customer satisfaction)
- **Composite Capabilities** - capability składające się z innych capabilities
