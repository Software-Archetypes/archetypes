# Więcej niż jedno zwierze, to... paczki produktów

## Wprowadzenie - po co pakiety?

Dzisiaj zajmiemy się problemem, który w prawdziwych systemach e-commerce jest absolutnie kluczowy. Mamy dobrze zdefiniowane produkty, znamy ich cechy, relacje, reguły aplikowalności. Ale świat biznesu nie sprzedaje pojedynczych klocków. Sprzedaje **zestawy, pakiety, bundle** - rzeczy które razem mają większy sens dla klienta i większą wartość dla biznesu.

Przykłady?

**W banku:** konto + karta + aplikacja + ubezpieczenie. Każdy element to osobny produkt, ale razem to "Pakiet Premium".

**W logistyce:** przesyłka + ubezpieczenie + powiadomienia SMS. Osobne usługi, ale sprzedawane jako jeden pakiet.

**W retailu:** laptop + torba + mysz + Office 365 + rozszerzona gwarancja. Pięć produktów, jeden "Complete Office Setup".

Każdy z tych elementów może być kupiony osobno. Ale pakiet daje **wygodę** (wszystko w jednym), **wartość** (często ze zniżką) i **spójność** (wiesz że to pasuje do siebie).

## Jak to się zazwyczaj robi? (anti-pattern)

Najczęściej widzę coś takiego:

```java
class BankAccount {
    String accountNumber;
    String creditCardId;      // dopisane - "pakiet" ma kartę
    String insurancePolicyId; // dopisane - może być z ubezpieczeniem
    String mobileAppId;       // dopisane - może mieć aplikację
}
```

Albo w przesyłkach:

```java
class ShippingPackage {
    String baseService = "Transport krajowy 24h";
    String insuranceId;
    String smsNotificationId;
}
```

**Problemy?**

1. **Brak spójności** - nikt nie wie które pole oznacza faktyczny związek, a które zostało dodane tylko do jednej promocji
2. **Brak elastyczności** - każda nowa kombinacja = zmiana w kodzie
3. **Trudność w utrzymaniu** - gdy jeden element pakietu się zmienia, cały model trzeba przebudować
4. **Błędy operacyjne** - różne cykle życia produktów w pakiecie, różne zespoły, różne systemy rozliczania

Pakiet to nie marketingowy skrót. **To strategiczny byt biznesowy.**

## Czego potrzebuje pakiet? Szkielet klasy

Zanim zaczniemy projektować, zastanówmy się czego faktycznie potrzebuje pakiet.

Spójrzmy na przykład "Laptop Bundle":
- Laptop (obowiązkowy, 1 sztuka)
- Pamięć RAM: wybierz 8GB, 16GB lub 32GB (obowiązkowy, 1 z 3)
- Dysk: wybierz 256GB, 512GB lub 1TB (obowiązkowy, 1 z 3)
- Akcesoria: mysz, torba, klawiatura (opcjonalnie, 0-2 sztuki)

Co z tego wynika?

```java
class Package {
    PackageId id;
    String name;
    String description;

    // Jakie produkty mogą być w pakiecie?
    List<ProductId> availableProducts;

    // Jakie są reguły wyboru?
    // - laptop: dokładnie 1
    // - pamięć: dokładnie 1 z {8GB, 16GB, 32GB}
    // - dysk: dokładnie 1 z {256GB, 512GB, 1TB}
    // - akcesoria: 0-2 z {mysz, torba, klawiatura}
    ???
}
```

Widzimy dwie rzeczy:
1. **Zbiory produktów** do wyboru ("Memory Options", "Storage Options", "Accessories")
2. **Reguły** mówiące ile i jakie można/trzeba wybrać

## Zestawienie z ProductType

Mamy już `ProductType`:

```java
class ProductType {
    ProductIdentifier id;
    ProductName name;
    ProductDescription description;
    Unit preferredUnit;
    ProductTrackingStrategy trackingStrategy;
    ProductFeatureTypes featureTypes;
    ProductMetadata metadata;
    ApplicabilityConstraint applicabilityConstraint;
}
```

A nasz szkielet `Package`:

```java
class Package {
    PackageId id;
    String name;
    String description;

    // Specyficzne dla pakietu:
    List<ProductSet> productSets;  // zbiory produktów
    List<SelectionRule> rules;     // reguły wyboru
}
```

**Pytanie:** Czy pakiet TO JEST produkt? Czy może pakiet MA produkt?

## Dyskusja: jak to zamodelować?

Pakiet **jest** produktem z perspektywy klienta i systemu sprzedaży:
- Ma swoją cenę
- Może być w koszyku
- Można go kupić
- Ma swój tracking (unikalne ID pakietu)
- Ma reguły aplikowalności (nie każdy może kupić pakiet premium)

Ale pakiet **różni się** od zwykłego produktu:
- Nie ma `unit` (zawsze "1 pakiet" = 1 sztuka)
- Nie ma `featureTypes` (choć komponenty mogą mieć)
- **Ma** strukturę komponentów (czego zwykły produkt nie ma)

### Opcja 1: Optional w ProductType

```java
class ProductType {
    // wszystkie pola jak dotychczas
    Optional<PackageStructure> packageStructure;
}
```

**Plusy:**
- Jeden typ w całym systemie
- Proste repository
- Nie trzeba instanceof

**Minusy:**
- Runtime check wszędzie: `productType.packageStructure().orElseThrow()`
- Każdy ProductType nosi Optional (95% pustych)
- Martwwe pola (unit, featureTypes) w pakietach

### Opcja 2: Dziedziczenie

```java
class PackageType extends ProductType {
    PackageStructure structure;
}
```

**Plusy:**
- Dziedziczy wspólne pola (id, name, metadata)
- Type safety

**Minusy:**
- ProductType ma pola których PackageType nie używa (unit, featureTypes)
- Nie wszystkie pola mają sens dla pakietu
- Konceptualnie: pakiet nie "rozszerza" produktu, to dwa równorzędne typy

### Opcja 3: Wspólny interfejs Product

```java
interface Product {
    ProductIdentifier id();
    ProductName name();
    ProductDescription description();
    ProductMetadata metadata();
    ApplicabilityConstraint applicabilityConstraint();

    default boolean isApplicableFor(ApplicabilityContext context) {
        return applicabilityConstraint().isSatisfiedBy(context);
    }
}

class ProductType implements Product {
    // id, name, description, metadata, applicabilityConstraint
    // + specyficzne: unit, trackingStrategy, featureTypes
}

class PackageType implements Product {
    // id, name, description, metadata, applicabilityConstraint
    // + specyficzne: trackingStrategy, structure
}
```

**Plusy:**
- Każda klasa ma tylko pola które faktycznie używa
- Polimorfizm - możesz pracować z `Product` nie wiedząc czy to pakiet czy nie
- Type safety - `PackageType` zawsze ma `structure()`
- Jasne rozróżnienie zachowań

**Minusy:**
- Duplikacja wspólnych pól w implementacjach (ale to standard w Javie)
- Trzeba instanceof gdy potrzebujesz specyficznych metod

### Nasza decyzja: Interfejs Product

Wybraliśmy **Opcję 3** - wspólny interfejs `Product` z dwiema implementacjami.

Dlaczego?

1. **Behavior matters more than data** - ProductType i PackageType różnią się nie tylko danymi, ale **zachowaniem**. Pricing pakietu to suma cen komponentów (może z rabatem). Walidacja pakietu to sprawdzanie reguł struktury. To fundamentalnie inne operacje niż dla zwykłego produktu.

2. **Clean model** - każda klasa ma tylko to co potrzebuje. ProductType ma `unit` i `featureTypes`. PackageType ma `structure`. Bez martwych pól.

3. **Extensibility** - łatwo dodać `SubscriptionProduct`, `BundleProduct`, `CompositeProduct` bez modyfikacji istniejących klas.

4. **Type safety** - metoda `validateSelection()` istnieje tylko w `PackageType`, nie w każdym produkcie.

```java
// Użycie polimorficzne
Product product = repository.findBy(productId);

if (product instanceof PackageType packageType) {
    PackageValidationResult result = packageType.validateSelection(selection);
}

// Albo pattern matching (Java 21+)
switch (product) {
    case ProductType pt -> handleRegularProduct(pt);
    case PackageType pkg -> handlePackage(pkg);
}
```

### Co się zmieniło w istniejącym modelu?

**UWAGA:** Wprowadzenie pakietów wymaga **refactoringu istniejącego kodu**. To nie jest tylko dodanie nowych klas - to zmiana struktury całego modelu.

#### Przed: ProductType jako standalone klasa

```java
class ProductType {
    ProductIdentifier id;
    ProductName name;
    ProductDescription description;
    Unit preferredUnit;
    ProductTrackingStrategy trackingStrategy;
    ProductFeatureTypes featureTypes;
    ProductMetadata metadata;
    ApplicabilityConstraint applicabilityConstraint;

    // Własny builder
    static class Builder { ... }
}

class ProductInstance {
    ProductInstanceId id;  // specyficzny dla ProductInstance
    ProductType productType;
    // ...

    // Własny builder
    static class Builder { ... }
}
```

Model był prosty: ProductType to samodzielna klasa, ProductInstance to jej instancja. Każdy miał swój builder wewnątrz.

#### Po: Product jako wspólny interfejs

```java
interface Product {
    ProductIdentifier id();
    ProductName name();
    ProductDescription description();
    ProductMetadata metadata();
    ApplicabilityConstraint applicabilityConstraint();
}

class ProductType implements Product {
    // te same pola jak wcześniej
    // USUNIĘTO: wewnętrzny Builder (przeniesiony do ProductBuilder)
}

class PackageType implements Product {
    // wspólne pola z Product
    // + specyficzne: structure
}
```

**Zmiana 1: ProductType implementuje Product**

Dodaliśmy interfejs, ProductType musi go implementować. To **backward compatible** - istniejący kod ProductType dalej działa, tylko dodajemy `implements Product` i `@Override` na metodach.

**Zmiana 2: Usunięto wewnętrzne buildery, dodano ProductBuilder**

Przed:
```java
ProductType laptop = ProductType.builder(id, name, description, unit, tracking)
    .withMandatoryFeature(colorFeature)
    .build();
```

Po:
```java
ProductType laptop = new ProductBuilder(id, name, description)
    .withMetadata("category", "electronics")
    .asProductType(unit, tracking)
        .withMandatoryFeature(colorFeature)
        .build();
```

To **breaking change**. Istniejący kod używający `ProductType.builder()` trzeba zmienić na `ProductBuilder`.

**Dlaczego?** Bo chcemy **jednego buildera** dla Product i Package, wzorując się na `TransactionBuilder` z Accounting.

#### Zmiana 3: Uwspólnienie identyfikatorów instancji

Przed:
```java
record ProductInstanceId(UUID value) {
    static ProductInstanceId newOne() { ... }
}

class ProductInstance {
    ProductInstanceId id;
    // ...
}
```

Po:
```java
record InstanceId(UUID value) {  // zmieniona nazwa!
    static InstanceId newOne() { ... }
}

interface Instance {
    InstanceId id();
    Product product();
    Optional<SerialNumber> serialNumber();
    Optional<BatchId> batchId();
}

class ProductInstance implements Instance {
    InstanceId id;  // było ProductInstanceId, jest InstanceId
    // ...
}

class PackageInstance implements Instance {
    InstanceId id;  // to samo ID co ProductInstance
    // ...
}
```

**Dlaczego uwspólnić ID?**

1. **Spójność** - skoro Product jest wspólnym interfejsem, Instance też powinien być
2. **Polimorfizm** - możesz mieć `Map<InstanceId, Instance>` niezależnie czy to ProductInstance czy PackageInstance
3. **Database** - jedna tabela instances z polymorphic associations
4. **Business logic** - "znajdź instancję o ID" nie musi wiedzieć czy to product czy package

To też **breaking change**. Istniejący kod używający `ProductInstanceId` musi zostać zmieniony na `InstanceId`.

**Zmiana 4: Usunięto buildery z ProductInstance, dodano InstanceBuilder**

Podobnie jak z ProductType, buildery przeniesione do wspólnego `InstanceBuilder`:

Przed:
```java
ProductInstance instance = ProductInstance.builder()
    .id(ProductInstanceId.newOne())
    .type(laptopType)
    .serial(serial)
    .build();
```

Po:
```java
ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
    .withSerial(serial)
    .asProductInstance(laptopType)
        .withQuantity(...)
        .build();
```

#### Podsumowanie zmian

| Co | Przed | Po | Breaking? |
|----|-------|-----|-----------|
| Typ bazowy | brak | `Product` interface | ✅ Non-breaking (tylko dodanie) |
| ProductType | standalone class | `implements Product` | ✅ Non-breaking |
| ProductType.builder | wewnętrzny Builder | `ProductBuilder` | ❌ **Breaking** |
| Instance ID | `ProductInstanceId` | `InstanceId` (wspólny) | ❌ **Breaking** |
| Instance bazowy | brak | `Instance` interface | ✅ Non-breaking (tylko dodanie) |
| ProductInstance | standalone class | `implements Instance` | ✅ Non-breaking |
| ProductInstance.builder | wewnętrzny Builder | `InstanceBuilder` | ❌ **Breaking** |

**Jak minimalizować impact?**

1. **Deprecated wrappers** - możesz zachować stare API jako deprecated:
```java
class ProductType implements Product {
    @Deprecated(forRemoval = true)
    static Builder builder(...) {
        // delegates to ProductBuilder
    }
}
```

2. **Migration guide** - jasna dokumentacja co i jak zmienić

3. **Testy** - upewnij się że wszystkie edge cases są pokryte przed i po migracji

**Wartość zmian:**

Breaking changes są **uzasadnione**, bo dają:
- ✅ Polimorfizm (Product, Instance)
- ✅ Spójność (jeden pattern dla typów i instancji)
- ✅ Elastyczność (łatwo dodać nowe typy produktów)
- ✅ Type safety (specjalizowane buildery)

Model stał się **bardziej dojrzały** kosztem kompatybilności. To uczciwy trade-off.

## Proste reguły: min/max wystarczy dla większości

W 90% przypadków pakiety mają proste reguły:
- "Wybierz dokładnie 1" (laptop, pamięć, dysk)
- "Wybierz 0 lub 1" (opcjonalne akcesoria)
- "Wybierz 2-4" (wybierz 2-4 dodatki)

Do tego wystarczą **liczby**:

```java
class SimplePackageRule {
    String setName;
    Set<ProductId> products;  // dostępne produkty
    int min;                  // minimum do wybrania
    int max;                  // maksimum do wybrania
}

// Przykład: pamięć - wybierz 1 z 3
SimplePackageRule memoryRule = new SimplePackageRule(
    "Memory",
    Set.of(ram8GB, ram16GB, ram32GB),
    1,  // min
    1   // max
);
```

Proste, czytelne, wystarczające.

Ale co jeśli biznes chce **więcej**?

## Zaawansowane reguły: gdy liczby nie wystarczają

Wyobraź sobie:

**Pakiet laptop z opcjami:**
- Jeśli wybierasz laptop **gamingowy**, musisz też wybrać dedykowaną kartę graficzną
- Jeśli wybierasz laptop **biznesowy**, możesz wybrać czytnik linii papilarnych lub moduł TPM

**Pakiet bankowy:**
- Jeśli konto **premium**, karta **musi być** premium
- Jeśli konto **basic**, karta **może być tylko** debetowa

To są **warunkowe zależności**: "jeśli A, to B".

Zwykłe min/max tego nie wyraża. Potrzebujesz logiki:

```
IF (gaming laptop selected) THEN (dedicated GPU required)
IF (business laptop selected) THEN (security module optional)
```

### Rozwiązanie: SelectionRule z kompozycją

Wprowadzamy dwa pojęcia:

**1. ProductSet** - zbiór produktów (dane)

```java
class ProductSet {
    String name;
    Set<ProductIdentifier> products;
}

// Przykład
ProductSet laptops = new ProductSet("Laptops", Set.of(gamingLaptop, businessLaptop));
ProductSet graphics = new ProductSet("Graphics", Set.of(dedicatedGPU));
ProductSet security = new ProductSet("Security", Set.of(fingerprintReader, tpmChip));
```

To jest pula możliwości - "składniki dostępne w kuchni".

**2. SelectionRule** - reguła wyboru (logika)

```java
interface SelectionRule {
    boolean isSatisfiedBy(List<SelectedProduct> selection);
}

// Podstawowa reguła: wybierz min-max z zestawu
record IsSubsetOf(ProductSet sourceSet, int min, int max)
    implements SelectionRule {

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        long count = selection.stream()
            .filter(s -> sourceSet.contains(s.productId()))
            .mapToInt(SelectedProduct::quantity)
            .sum();

        return count >= min && count <= max;
    }
}
```

To jest "przepis": "użyj 1 mięsa", "dodaj 2-3 warzywa".

### Kompozycja: AND, OR, IF-THEN

Kluczowa moc: reguły można **komponować**.

**AND - wszystkie muszą być spełnione:**

```java
record AndRule(List<SelectionRule> rules) implements SelectionRule {
    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        return rules.stream().allMatch(r -> r.isSatisfiedBy(selection));
    }
}

// Użycie: musisz wybrać laptop AND pamięć AND dysk
SelectionRule packageRules = new AndRule(List.of(
    new IsSubsetOf(laptopSet, 1, 1),
    new IsSubsetOf(memorySet, 1, 1),
    new IsSubsetOf(storageSet, 1, 1)
));
```

**OR - przynajmniej jedna musi być spełniona:**

```java
record OrRule(List<SelectionRule> rules) implements SelectionRule {
    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        return rules.stream().anyMatch(r -> r.isSatisfiedBy(selection));
    }
}
```

**IF-THEN - warunkowa reguła:**

```java
record ConditionalRule(
    SelectionRule condition,
    List<SelectionRule> thenRules
) implements SelectionRule {

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        if (condition.isSatisfiedBy(selection)) {
            // Jeśli warunek spełniony, wszystkie then-rules muszą być spełnione
            return thenRules.stream().allMatch(r -> r.isSatisfiedBy(selection));
        }
        // Jeśli warunek nie spełniony, reguła przechodzi automatycznie
        return true;
    }
}

// Przykład: IF gaming laptop THEN GPU required
ProductSet gamingOnly = new ProductSet("GamingLaptop", Set.of(gamingLaptop));
SelectionRule conditionalGPU = new ConditionalRule(
    new IsSubsetOf(gamingOnly, 1, 1),  // condition: gaming laptop selected
    List.of(
        new IsSubsetOf(graphicsSet, 1, 1)  // then: GPU required
    )
);
```

**Bez warunkowych reguł** musiałbyś mieć **dwa oddzielne pakiety**:
- Gaming Bundle (laptop + GPU)
- Business Bundle (laptop + security)

**Z warunkowymi regułami** masz **jeden elastyczny pakiet** gdzie wybór determinuje wymagania.

### PackageStructure: ProductSets + SelectionRules

Łączymy to razem:

```java
class PackageStructure {
    Map<String, ProductSet> productSets;      // dostępne zbiory produktów
    List<SelectionRule> selectionRules;       // reguły wyboru

    PackageValidationResult validate(List<SelectedProduct> selection) {
        List<String> errors = new ArrayList<>();

        for (SelectionRule rule : selectionRules) {
            if (!rule.isSatisfiedBy(selection)) {
                errors.add("Rule not satisfied: " + rule);
            }
        }

        return errors.isEmpty()
            ? PackageValidationResult.success()
            : PackageValidationResult.failure(errors);
    }
}
```

ProductSets to **dane** (jakie produkty są dostępne).
SelectionRules to **logika** (jakie kombinacje są dozwolone).

## Composite Pattern: paczki w paczkach

Ostatni element układanki: **pakiet może zawierać pakiety**.

Przykład:
```
Complete Office Setup (pakiet)
├─ Hardware Package (pakiet)
│  ├─ Laptop
│  └─ Monitor
├─ Software Package (pakiet)
│  ├─ Office 365
│  └─ Antivirus
└─ Support Package (pakiet)
   ├─ Installation Service
   └─ Training
```

To jest klasyczny **Composite Pattern**:
- **Product** - interfejs (component)
- **ProductType** - liść (leaf)
- **PackageType** - kompozyt (composite), może zawierać inne Products

ProductSet może zawierać zarówno ProductType jak i PackageType:

```java
ProductSet computerSet = new ProductSet("Computer",
    Set.of(hardwarePackage.id())  // to jest PackageType!
);

ProductSet softwareSet = new ProductSet("Software",
    Set.of(softwarePackage.id())  // to też PackageType!
);

PackageType completeSetup = PackageType.define(
    setupId,
    "Complete Office Setup",
    "Everything you need",
    new PackageStructure(
        Map.of("Computer", computerSet, "Software", softwareSet),
        List.of(
            new IsSubsetOf(computerSet, 1, 1),
            new IsSubsetOf(softwareSet, 1, 1)
        )
    )
);
```

**Zagnieżdżanie wymaga ostrożności:**
1. **Walidacja przeciw cyklom** - Package A nie może zawierać Package B który zawiera Package A
2. **Limit głębokości** - maksymalnie 3-5 poziomów zagnieżdżenia
3. **Rekurencyjne operacje** - pricing, walidacja działają rekurencyjnie

## Builder Pattern: fluent API

Ostatni element to wygodne tworzenie pakietów. Wzorując się na `TransactionBuilder` z modułu Accounting, stworzyliśmy `ProductBuilder` ze specjalizowanymi inner builderami.

**Wspólne atrybuty w głównym builderze:**

```java
ProductBuilder builder = new ProductBuilder(id, name, description)
    .withMetadata("category", "electronics")
    .withMetadata("promotion", "summer2025")
    .withApplicabilityConstraint(
        ApplicabilityConstraint.greaterThan("monthlyIncome", 5000)
    );
```

**Dla zwykłego produktu - asProductType():**

```java
ProductType laptop = builder
    .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
        .withMandatoryFeature(ProductFeatureType.of("color"))
        .withOptionalFeature(ProductFeatureType.of("storage"))
        .build();
```

**Dla pakietu - asPackage() z fluent API:**

```java
PackageType laptopBundle = builder
    .asPackage()
        .withSingleChoice("Laptop", laptop.id())
        .withSingleChoice("Memory", ram8GB.id(), ram16GB.id(), ram32GB.id())
        .withSingleChoice("Storage", ssd256GB.id(), ssd512GB.id(), ssd1TB.id())
        .withOptionalChoice("Accessories", mouse.id(), bag.id())
        .build();
```

Helper methods:
- `withSingleChoice()` - dokładnie 1 (min=1, max=1)
- `withOptionalChoice()` - 0 lub 1 (min=0, max=1)
- `withRequiredChoice()` - przynajmniej 1 (min=1, max=unlimited)
- `withChoice(name, min, max, ...)` - custom min/max

**Dla zaawansowanych przypadków z warunkową regułą:**

```java
PackageType gamingBundle = new ProductBuilder(id, name, description)
    .asPackage()
        .withSingleChoice("Laptops", businessLaptop.id(), gamingLaptop.id())
        .withOptionalChoice("Graphics", dedicatedGPU.id())
        // Dodaj warunkową regułę ręcznie
        .withProposition(
            SelectionRule.ifThen(
                SelectionRule.single(
                    new ProductSet("GamingLaptop", Set.of(gamingLaptop.id()))
                ),
                SelectionRule.single(
                    new ProductSet("GPU", Set.of(dedicatedGPU.id()))
                )
            )
        )
        .build();
```

Builder pattern ma kluczową zaletę: **różne metody kreacyjne zwracają różne typy builderów**.

`.asProductType()` → `ProductTypeBuilder` (ma `withMandatoryFeature()`)
`.asPackage()` → `PackageTypeBuilder` (ma `withSingleChoice()`)

Każdy builder ma tylko metody które mają sens dla danego typu.

## Podsumowanie

Zbudowaliśmy kompletny system pakietów produktowych:

1. **Wspólny interfejs Product** - ProductType i PackageType implementują ten sam kontrakt
2. **ProductSet** - zbiory produktów dostępnych w pakiecie (dane)
3. **SelectionRule** - reguły definiujące dozwolone kombinacje (logika)
   - Podstawowe: min/max selection
   - Kompozycja: AND, OR
   - Warunkowe: IF-THEN
4. **PackageStructure** - łączy ProductSets i SelectionRules
5. **Composite Pattern** - pakiety mogą zawierać pakiety
6. **Builder Pattern** - fluent API ze specjalizowanymi builderami

**Kluczowe decyzje projektowe:**

✅ **Interface over inheritance** - Product jako interfejs, nie abstract class. Każda implementacja ma tylko to czego potrzebuje.

✅ **Separation of data and logic** - ProductSet (co jest dostępne) vs SelectionRule (jakie są ograniczenia).

✅ **Composition over configuration** - SelectionRules komponują się (AND/OR/IF-THEN), nie sztywne enum czy string-based rules.

✅ **Fluent API** - builder ze specjalizowanymi metodami dla każdego typu (`asProductType()`, `asPackage()`).

**Wartość biznesowa:**

Pakiety nie są marketingowym dodatkiem. To **strategiczny mechanizm** pozwalający:
- Zwiększać wartość koszyka (bundle discount, convenience)
- Upraszczać wybór klienta (predefiniowane zestawy)
- Zarządzać złożonością oferty (jeden pakiet zamiast kombinatoryki produktów)
- Kontrolować jakie kombinacje są dozwolone (warunkowe reguły)

System który modeluje pakiety explicite **rozumie biznes** zamiast go obchodzić.

## Od typu do instancji: co klient faktycznie kupił?

Do tej pory budowaliśmy **typy** produktów i pakietów:
- `ProductType` - "iPhone 15 Pro 256GB"
- `PackageType` - "Laptop Bundle (wybierz RAM, dysk, akcesoria)"

Ale **klient nie kupuje typu**. Klient kupuje **konkretną rzecz**:
- Konkretny iPhone z serialem ABC123
- Konkretny pakiet laptop bundle gdzie wybrał: 16GB RAM, 512GB SSD, mysz

**Typ** to szablon. **Instancja** to egzemplarz.

W systemach e-commerce ten rozdział jest kluczowy:
- **Typ** definiuje co można sprzedać
- **Instancja** to co faktycznie sprzedano

### Instancje: wspólny interfejs jak przy Product

Podobnie jak `Product` jest wspólnym interfejsem dla `ProductType` i `PackageType`, tworzymy `Instance` jako wspólny interfejs dla instancji:

```java
interface Instance {
    InstanceId id();
    Product product();  // może zwracać ProductType lub PackageType
    Optional<SerialNumber> serialNumber();
    Optional<BatchId> batchId();
}
```

**Dlaczego wspólne ID?**

Każda instancja potrzebuje unikalnego identyfikatora niezależnie od tego czy ma serial number czy batch:
- Serial number jest opcjonalny (nie wszystkie produkty są śledzone indywidualnie)
- Batch jest opcjonalny (nie wszystkie produkty są śledzone grupowo)
- Przynajmniej jedno z nich **musi** istnieć (zgodnie z tracking strategy produktu)
- Ale do persistence potrzebujemy **stałego primary key** - to jest `InstanceId`

### ProductInstance: konkretny egzemplarz produktu

```java
class ProductInstance implements Instance {
    private final InstanceId id;
    private final ProductType productType;
    private final SerialNumber serialNumber;
    private final BatchId batchId;
    private final Quantity quantity;
    private final ProductFeatureInstances features;

    // Przykład użycia:
    ProductInstance myLaptop = new ProductInstance(
        InstanceId.newOne(),
        laptopType,
        SerialNumber.of("SN-ABC123"),
        null,  // brak batch
        null,  // quantity implicit 1 piece
        features
    );
}
```

ProductInstance to:
- **Konkretny egzemplarz** ProductType (ten konkretny laptop, ta konkretna książka)
- Ma **tracking** (serial number lub batch, zgodnie z tracking strategy produktu)
- Może mieć **quantity** (np. 8.5 godziny konsultingu, 3.2kg mięsa)
- Ma **feature instances** (kolor=srebrny, dysk=512GB, rok=2024)

### PackageInstance: konkretny pakiet z wyborami klienta

```java
class PackageInstance implements Instance {
    private final InstanceId id;
    private final PackageType packageType;
    private final List<SelectedProduct> selection;
    private final SerialNumber serialNumber;
    private final BatchId batchId;

    // Przykład użycia:
    PackageInstance customerBundle = new PackageInstance(
        InstanceId.newOne(),
        laptopBundleType,
        List.of(
            new SelectedProduct(ram16GB.id(), 1),
            new SelectedProduct(ssd512GB.id(), 1),
            new SelectedProduct(mouse.id(), 1)
        ),
        null,  // brak serial
        BatchId.of("BUNDLE-2025-001")
    );
}
```

PackageInstance to:
- **Konkretny pakiet** sprzedany klientowi
- Z **konkretnymi wyborami** komponentów (klient wybrał 16GB, nie 8GB)
- Selection jest **walidowana** przy tworzeniu (musi spełniać SelectionRules pakietu)
- Ma swój tracking (serial lub batch dla całego pakietu)

**Kluczowa różnica:**
- ProductInstance ma **feature instances** (wartości cech produktu)
- PackageInstance ma **selection** (które produkty klient wybrał z dostępnych opcji)

### InstanceBuilder: spójność z ProductBuilder

Podobnie jak `ProductBuilder` tworzy ProductType lub PackageType, `InstanceBuilder` tworzy ProductInstance lub PackageInstance:

```java
// ProductInstance
ProductInstance laptop = new InstanceBuilder(InstanceId.newOne())
    .withSerial(SerialNumber.of("ABC123"))
    .asProductInstance(laptopType)
        .withQuantity(Quantity.of(1, Unit.pieces()))
        .withFeature(colorFeature, "silver")
        .withFeature(storageFeature, "512GB")
        .build();

// PackageInstance
PackageInstance bundle = new InstanceBuilder(InstanceId.newOne())
    .withBatch(BatchId.of("BATCH-001"))
    .asPackageInstance(laptopBundleType)
        .withSelection(List.of(
            new SelectedProduct(ram16GB.id(), 1),
            new SelectedProduct(ssd512GB.id(), 1),
            new SelectedProduct(mouse.id(), 1)
        ))
        .build();
```

**Spójność modelu:**

| Co definiujemy | Co sprzedajemy |
|---------------|----------------|
| `Product` (interface) | `Instance` (interface) |
| `ProductType` (typ produktu) | `ProductInstance` (egzemplarz) |
| `PackageType` (typ pakietu) | `PackageInstance` (konkretny pakiet) |
| `ProductBuilder` (builder typów) | `InstanceBuilder` (builder instancji) |

Ten paralelizm sprawia, że model jest **intuicyjny** i **łatwy w rozszerzaniu**.

## Relacje między produktami i pakietami

Ostatni element układanki: **relacje**.

Wcześniejsza lekcja pokazywała relacje między produktami (UPGRADABLE_TO, SUBSTITUTED_BY, REPLACED_BY, COMPLEMENTED_BY, COMPATIBLE_WITH, INCOMPATIBLE_WITH).

Kluczowe pytanie: **czy relacje działają też dla pakietów?**

Odpowiedź: **TAK**, i to bardzo naturalnie.

### ProductRelationship działa na ProductIdentifier

```java
record ProductRelationship(
    ProductRelationshipId id,
    ProductIdentifier from,
    ProductIdentifier to,
    ProductRelationshipType type
)
```

Ponieważ zarówno `ProductType` jak i `PackageType` implementują `Product` i mają `ProductIdentifier`, relacje mogą być:

**1. ProductType → ProductType** (klasyczne relacje między produktami)

```java
// Upgrade między wersjami laptopa
ProductRelationship upgrade = ProductRelationship.of(
    id,
    laptop2023.id(),
    laptop2024.id(),
    ProductRelationshipType.UPGRADABLE_TO
);

// Substytucja (ten sam smartphone ale u innego producenta)
ProductRelationship substitute = ProductRelationship.of(
    id,
    samsungPhone.id(),
    applePhone.id(),
    ProductRelationshipType.SUBSTITUTED_BY
);
```

**2. PackageType → PackageType** (relacje między pakietami)

```java
// Upgrade pakietu bankowego
ProductRelationship packageUpgrade = ProductRelationship.of(
    id,
    basicBankingPackage.id(),
    premiumBankingPackage.id(),
    ProductRelationshipType.UPGRADABLE_TO
);

// Replacement pakietu (nowa oferta zastępuje starą)
ProductRelationship packageReplacement = ProductRelationship.of(
    id,
    summerBundle2024.id(),
    summerBundle2025.id(),
    ProductRelationshipType.REPLACED_BY
);
```

**3. ProductType → PackageType** (upgrade z produktu do pakietu)

```java
// Klient kupił samego laptopa, można mu zaproponować upgrade do bundle
ProductRelationship upsell = ProductRelationship.of(
    id,
    laptop.id(),
    laptopBundle.id(),
    ProductRelationshipType.UPGRADABLE_TO
);

// Laptop jest komplementarny do pakietu akcesoriów
ProductRelationship complement = ProductRelationship.of(
    id,
    laptop.id(),
    accessoriesBundle.id(),
    ProductRelationshipType.COMPLEMENTED_BY
);
```

**4. PackageType → ProductType** (downgrade z pakietu do pojedynczego produktu)

```java
// Klient może zrezygnować z pakietu i zostać tylko z podstawowym produktem
ProductRelationship downgrade = ProductRelationship.of(
    id,
    premiumBundle.id(),
    basicLaptop.id(),
    ProductRelationshipType.SUBSTITUTED_BY
);
```

### Przykłady biznesowe

**Banking:**
```java
// Basic Account (ProductType) → Premium Package (PackageType)
// Klient z kontem basic może upgrade'ować do pakietu premium (konto + karta + ubezpieczenie)
ProductRelationship.of(id, basicAccount.id(), premiumPackage.id(), UPGRADABLE_TO);

// Premium Package → VIP Package (oba PackageType)
ProductRelationship.of(id, premiumPackage.id(), vipPackage.id(), UPGRADABLE_TO);
```

**E-commerce:**
```java
// Laptop (ProductType) → Complete Office Setup (PackageType)
// Laptop jest częścią większego pakietu biurowego
ProductRelationship.of(id, laptop.id(), officeSetup.id(), COMPLEMENTED_BY);

// Gaming Bundle 2024 → Gaming Bundle 2025 (oba PackageType)
ProductRelationship.of(id, gamingBundle2024.id(), gamingBundle2025.id(), REPLACED_BY);
```

**Logistics:**
```java
// Basic Shipping (ProductType) → Express Package (PackageType)
// Podstawowa przesyłka może być upgrade'owana do pakietu express z ubezpieczeniem i SMS
ProductRelationship.of(id, basicShipping.id(), expressPackage.id(), UPGRADABLE_TO);
```

### Dlaczego to ma sens?

1. **UPGRADABLE_TO** - naturalnie działa między wszystkim co ma ProductIdentifier. Klient może upgrade'ować:
   - Z produktu do lepszego produktu
   - Z produktu do pakietu (upsell!)
   - Z pakietu do lepszego pakietu

2. **COMPLEMENTED_BY** - laptop jest komplementarny do bundle akcesoriów, konto bankowe jest komplementarne do pakietu ubezpieczeniowego

3. **SUBSTITUTED_BY / REPLACED_BY** - nowa oferta zastępuje starą, niezależnie czy to produkty czy pakiety

4. **COMPATIBLE_WITH / INCOMPATIBLE_WITH** - niektóre pakiety nie mogą być kupione razem (np. "Package A incompatible with Package B")

### Walidacja i kontrola

Nie wszystkie relacje mają sens w każdym kontekście. System może:

```java
// Walidacja czy relacja ma sens
if (from instanceof PackageType && to instanceof PackageType) {
    if (type == COMPLEMENTED_BY) {
        // Uwaga: pakiet komplementarny do pakietu - czy to sensowne?
        logger.warn("Unusual relationship: package complemented by package");
    }
}

// Business rules mogą ograniczać relacje
RelationshipValidator validator = new RelationshipValidator();
validator.validate(relationship);  // może rzucić wyjątek jeśli relacja nie ma sensu
```

Ale w większości przypadków relacje działają **uniwersalnie** - bo `Product` jest wspólnym interfejsem.

## Podsumowanie rozszerzone

Zbudowaliśmy kompletny system produktów i pakietów z instancjami i relacjami:

**Typy (co można sprzedać):**
1. `Product` - wspólny interfejs
2. `ProductType` - konkretny typ produktu
3. `PackageType` - typ pakietu z SelectionRules
4. `ProductBuilder` - fluent API do tworzenia typów

**Instancje (co faktycznie sprzedano):**
5. `Instance` - wspólny interfejs
6. `ProductInstance` - konkretny egzemplarz produktu
7. `PackageInstance` - konkretny pakiet z wyborami klienta
8. `InstanceBuilder` - fluent API do tworzenia instancji

**Struktury pakietów:**
9. `ProductSet` - zbiory produktów (dane)
10. `SelectionRule` - reguły wyboru (logika) z kompozycją (AND/OR/IF-THEN)
11. `PackageStructure` - łączy ProductSets i SelectionRules

**Relacje:**
12. `ProductRelationship` - działa uniwersalnie między wszystkimi Product (ProductType ↔ PackageType ↔ PackageType)

**Wzorce projektowe:**
- **Composite Pattern** - Product (component), ProductType (leaf), PackageType (composite)
- **Composite Pattern** - Instance (component), ProductInstance (leaf), PackageInstance (composite)
- **Builder Pattern** - ProductBuilder i InstanceBuilder ze specjalizowanymi inner builderami
- **Strategy Pattern** - SelectionRule z różnymi implementacjami
- **Composite Pattern w logice** - SelectionRules komponują się (AND/OR/IF-THEN)

**Kluczowe decyzje:**

✅ **Paralelizm** - struktura Product/Instance jest symetryczna i intuicyjna

✅ **Uniwersalność** - relacje działają między wszystkimi typami Product bez specjalnych przypadków

✅ **Type safety** - każdy builder zwraca specjalizowany typ z odpowiednimi metodami

✅ **Separation of concerns** - dane (ProductSet) oddzielone od logiki (SelectionRule)

**Wartość biznesowa:**

System który explicite modeluje:
- **Typy i instancje** - rozumie różnicę między szablonem a sprzedażą
- **Pakiety** - wie że bundle to nie lista produktów, to strategiczny byt
- **Relacje uniwersalne** - upgrade z produktu do pakietu = upsell opportunity
- **Warunkowe reguły** - elastyczne pakiety zamiast kombinatoryki

To nie jest system sprzedażowy. To jest **model domeny e-commerce** który rozumie biznes.
