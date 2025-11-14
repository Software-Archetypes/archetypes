# Od wyboru do dostawy: PackageInstance w akcji

## Wprowadzenie: Szablon vs Realizacja

Do tej pory budowaliśmy **szablony** - `PackageType` to definicja tego co można sprzedać:
- Jakie produkty są dostępne (ProductSets)
- Jakie są reguły wyboru (SelectionRules)
- Co klient może skonfigurować

Ale biznes dzieje się w momencie **sprzedaży**. Klient nie kupuje szablonu. Klient kupuje **konkretny pakiet** z **konkretnymi wyborami** i **konkretnymi egzemplarzami**.

**Analogia:**
- **PackageType** = Formularz zamówienia pizzy ("Wybierz rozmiar: S/M/L, dodatki: 0-3")
- **PackageInstance** = Twoja konkretna pizza z magazynu ("Large, pepperoni, extra cheese, pieczarki, pudełko #4523")

**W systemie e-commerce:**
```
Customer Order             →  Warehouse Fulfillment      →  Delivery
┌─────────────────┐           ┌──────────────────────┐      ┌────────────────────┐
│ Wybór TYPÓW     │           │ Przydzielenie        │      │ Dostarczone        │
│ produktów       │           │ EGZEMPLARZY          │      │ EGZEMPLARZE        │
│                 │           │                      │      │                    │
│ SelectedProduct │    →      │ SelectedInstance     │  →   │ PackageInstance    │
│ (ProductId)     │           │ (Instance)           │      │ (persisted)        │
└─────────────────┘           └──────────────────────┘      └────────────────────┘
```

## Scenariusz biznesowy: Telekomunikacja

### Kontekst biznesowy

Operator telekomunikacyjny **Telco Plus** oferuje pakiet **"5G Starter Pack"**:

**Co zawiera:**
1. **Smartfon** - klient wybiera jeden z trzech modeli
2. **Karta SIM** - klient dostaje nową kartę z przydzielonym numerem
3. **Plan taryfowy** - klient wybiera jeden z planów (Basic/Premium/Unlimited)
4. **Usługi dodatkowe** - opcjonalnie 0-2 usługi (roaming, tethering, family share)

**Reguły biznesowe:**
- IF wybrano Plan Unlimited → THEN tethering jest ZABRONIONY (już wliczony)
- IF wybrano Smartfon Premium → THEN musi wybrać Plan Premium lub Unlimited

**Dzisiaj zobaczymy pełny cykl życia:** od momentu gdy klient Anna Kowalska składa zamówienie, przez realizację w magazynie, aż po dostarczenie konkretnych egzemplarzy.

---

## Krok 1: Definicja PackageType (szablon)

Zacznijmy od zdefiniowania szablonu pakietu - **co można wybrać i jakie są reguły**.

```java
public class TelcoPackageSetup {

    // ========== PRODUKTY: SMARTFONY ==========

    // Każdy smartfon to ProductType - SZABLON produktu
    private final ProductType phoneBasic = new ProductBuilder(
        ProductIdentifier.of("PHONE-BASIC-001"),
        ProductName.of("SmartPhone Basic 5G"),
        ProductDescription.of("Entry-level 5G smartphone, 64GB storage")
    )
        .withMetadata("category", "smartphone")
        .withMetadata("tier", "basic")
        .withMetadata("storage", "64GB")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    private final ProductType phoneStandard = new ProductBuilder(
        ProductIdentifier.of("PHONE-STD-001"),
        ProductName.of("SmartPhone Standard 5G"),
        ProductDescription.of("Mid-range 5G smartphone, 128GB storage")
    )
        .withMetadata("category", "smartphone")
        .withMetadata("tier", "standard")
        .withMetadata("storage", "128GB")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    private final ProductType phonePremium = new ProductBuilder(
        ProductIdentifier.of("PHONE-PREM-001"),
        ProductName.of("SmartPhone Premium 5G"),
        ProductDescription.of("Flagship 5G smartphone, 256GB storage, premium camera")
    )
        .withMetadata("category", "smartphone")
        .withMetadata("tier", "premium")
        .withMetadata("storage", "256GB")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    // ========== PRODUKTY: KARTY SIM ==========

    // Karta SIM - każda karta będzie miała unikalny IMSI i przydzielony numer
    private final ProductType simCard = new ProductBuilder(
        ProductIdentifier.of("SIM-5G-001"),
        ProductName.of("5G SIM Card"),
        ProductDescription.of("5G-capable SIM card with unique IMSI")
    )
        .withMetadata("category", "sim")
        .withMetadata("technology", "5G")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            // Każda karta ma serial number (IMSI)
            .build();

    // ========== PRODUKTY: PLANY TARYFOWE ==========

    private final ProductType planBasic = new ProductBuilder(
        ProductIdentifier.of("PLAN-BASIC-001"),
        ProductName.of("5G Basic Plan"),
        ProductDescription.of("10GB data, unlimited calls & SMS")
    )
        .withMetadata("category", "plan")
        .withMetadata("dataLimit", "10GB")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType planPremium = new ProductBuilder(
        ProductIdentifier.of("PLAN-PREM-001"),
        ProductName.of("5G Premium Plan"),
        ProductDescription.of("50GB data, unlimited calls & SMS, HD streaming")
    )
        .withMetadata("category", "plan")
        .withMetadata("dataLimit", "50GB")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType planUnlimited = new ProductBuilder(
        ProductIdentifier.of("PLAN-UNLIM-001"),
        ProductName.of("5G Unlimited Plan"),
        ProductDescription.of("Unlimited data, calls & SMS, tethering included")
    )
        .withMetadata("category", "plan")
        .withMetadata("dataLimit", "unlimited")
        .withMetadata("tethering", "included")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // ========== PRODUKTY: USŁUGI DODATKOWE ==========

    private final ProductType roamingService = new ProductBuilder(
        ProductIdentifier.of("SVC-ROAM-001"),
        ProductName.of("International Roaming"),
        ProductDescription.of("Use your plan abroad in 100+ countries")
    )
        .withMetadata("category", "service")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType tetheringService = new ProductBuilder(
        ProductIdentifier.of("SVC-TETHER-001"),
        ProductName.of("Mobile Tethering"),
        ProductDescription.of("Share your mobile data with other devices")
    )
        .withMetadata("category", "service")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType familyShareService = new ProductBuilder(
        ProductIdentifier.of("SVC-FAMILY-001"),
        ProductName.of("Family Share"),
        ProductDescription.of("Share data allowance with up to 4 family members")
    )
        .withMetadata("category", "service")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // ========== DEFINICJA PRODUCTSETS (grupy produktów) ==========

    private final ProductSet smartphonesSet = new ProductSet("Smartphones",
        Set.of(phoneBasic.id(), phoneStandard.id(), phonePremium.id()));

    private final ProductSet simCardsSet = new ProductSet("SIMCards",
        Set.of(simCard.id()));

    private final ProductSet plansSet = new ProductSet("Plans",
        Set.of(planBasic.id(), planPremium.id(), planUnlimited.id()));

    private final ProductSet servicesSet = new ProductSet("AdditionalServices",
        Set.of(roamingService.id(), tetheringService.id(), familyShareService.id()));

    // ProductSets dla reguł biznesowych
    private final ProductSet premiumPhones = new ProductSet("PremiumPhones",
        Set.of(phonePremium.id()));

    private final ProductSet premiumPlans = new ProductSet("PremiumPlans",
        Set.of(planPremium.id(), planUnlimited.id()));

    private final ProductSet unlimitedPlans = new ProductSet("UnlimitedPlans",
        Set.of(planUnlimited.id()));

    private final ProductSet tetheringServiceSet = new ProductSet("TetheringService",
        Set.of(tetheringService.id()));

    // ========== REGUŁY BIZNESOWE ==========

    // Reguła 1: IF Premium Phone THEN Premium/Unlimited Plan
    private final SelectionRule premiumPhoneRule = SelectionRule.ifThen(
        SelectionRule.required(premiumPhones),  // condition: wybrał premium phone
        SelectionRule.or(                       // then: musi wybrać premium lub unlimited plan
            SelectionRule.required(premiumPlans)
        )
    );

    // Reguła 2: IF Unlimited Plan THEN NOT Tethering Service
    // (bo tethering już jest wliczony w Unlimited)
    private final SelectionRule unlimitedTetheringRule = SelectionRule.ifThen(
        SelectionRule.required(unlimitedPlans),            // condition: wybrał unlimited
        SelectionRule.not(                                 // then: NIE MOŻE wybrać tethering
            SelectionRule.required(tetheringServiceSet)
        )
    );

    // ========== PACKAGETYPE: SZABLON PAKIETU ==========

    public PackageType create5GStarterPack() {
        return new ProductBuilder(
            ProductIdentifier.of("PKG-5G-STARTER"),
            ProductName.of("5G Starter Pack"),
            ProductDescription.of("Complete 5G package: phone + SIM + plan + optional services")
        )
            .withMetadata("category", "telecom")
            .withMetadata("targetSegment", "new-customers")
            .withApplicabilityConstraint(
                ApplicabilityConstraint.equals("customerType", "new")
            )
            .asPackage()
                // Wybór smartfona: dokładnie 1
                .withSingleChoice("Smartphone",
                    phoneBasic.id(),
                    phoneStandard.id(),
                    phonePremium.id()
                )
                // Karta SIM: dokładnie 1 (zawsze wymagana)
                .withSingleChoice("SIMCard", simCard.id())

                // Plan taryfowy: dokładnie 1
                .withSingleChoice("Plan",
                    planBasic.id(),
                    planPremium.id(),
                    planUnlimited.id()
                )

                // Usługi dodatkowe: 0-2
                .withChoice("Services", 0, 2,
                    roamingService.id(),
                    tetheringService.id(),
                    familyShareService.id()
                )

                // Dodajemy zaawansowane reguły biznesowe
                .withSelectionRule(premiumPhoneRule)
                .withSelectionRule(unlimitedTetheringRule)
                .build();
    }
}
```

**Co się stało:**
1. ✅ Zdefiniowaliśmy wszystkie ProductType (smartfony, SIM, plany, usługi)
2. ✅ Pogrupowaliśmy je w ProductSets (Smartphones, Plans, Services, etc.)
3. ✅ Stworzyliśmy reguły biznesowe (Premium Phone → Premium Plan, Unlimited → NO Tethering)
4. ✅ Zbudowaliśmy PackageType "5G Starter Pack" - **SZABLON** pakietu

Ten pakiet jest teraz dostępny w katalogu. Klienci mogą go zobaczyć w sklepie online lub w salonie operatora.

---

## Krok 2: Klient składa zamówienie (Order)

**15 grudnia 2025, godz. 14:30**
Anna Kowalska wchodzi na stronę Telco Plus. Widzi "5G Starter Pack" i konfiguruje swój pakiet.

```java
public class CustomerOrderCreation {

    public void annaConfiguresHerPackage() {
        // Anna widzi PackageType i konfiguruje swój wybór
        PackageType starterPack = packageRepository.findById(
            ProductIdentifier.of("PKG-5G-STARTER")
        );

        // WYBORY ANNY (typy produktów, nie konkretne egzemplarze!)
        // Anna wybiera:
        // - SmartPhone Standard 5G (nie Basic, nie Premium - środkowy model)
        // - SIM Card (zawsze wymagana)
        // - Plan Premium (50GB, chce więcej niż Basic)
        // - Roaming + Family Share (2 usługi dodatkowe)

        List<SelectedProduct> annaSelection = List.of(
            new SelectedProduct(
                ProductIdentifier.of("PHONE-STD-001"),  // Standard phone
                1
            ),
            new SelectedProduct(
                ProductIdentifier.of("SIM-5G-001"),     // SIM card
                1
            ),
            new SelectedProduct(
                ProductIdentifier.of("PLAN-PREM-001"),  // Premium plan
                1
            ),
            new SelectedProduct(
                ProductIdentifier.of("SVC-ROAM-001"),   // Roaming service
                1
            ),
            new SelectedProduct(
                ProductIdentifier.of("SVC-FAMILY-001"), // Family share service
                1
            )
        );

        // ========== WALIDACJA: Czy wybór spełnia reguły pakietu? ==========

        // System automatycznie sprawdza wszystkie SelectionRules:
        // 1. Czy wybrała dokładnie 1 smartfon? ✓
        // 2. Czy wybrała dokładnie 1 SIM? ✓
        // 3. Czy wybrała dokładnie 1 plan? ✓
        // 4. Czy wybrała 0-2 usługi? ✓ (2 usługi)
        // 5. IF Premium Phone THEN Premium Plan? N/A (nie wybrała premium phone)
        // 6. IF Unlimited THEN NOT Tethering? N/A (nie wybrała unlimited)

        PackageValidationResult validation = starterPack.validateSelection(annaSelection);

        if (!validation.isValid()) {
            // Gdyby coś było nie tak, system pokazałby błędy
            throw new InvalidSelectionException(
                "Your selection is invalid: " + validation.errors()
            );
        }

        // ✅ Walidacja przeszła! Zapisujemy zamówienie

        // ========== TWORZENIE ZAMÓWIENIA ==========

        Order annaOrder = new Order(
            OrderId.of("ORD-2025-12-15-0001423"),
            CustomerId.of("CUST-ANNA-KOWALSKA"),
            starterPack,                // PackageType który zamówiła
            annaSelection,              // Co wybrała (TYPY produktów)
            LocalDateTime.now(),        // 2025-12-15 14:30
            OrderStatus.PENDING         // Zamówienie czeka na realizację
        );

        orderRepository.save(annaOrder);

        System.out.println("Order created for Anna Kowalska: " + annaOrder.id());
        System.out.println("Selected products: " + annaSelection.size());
        System.out.println("Status: PENDING - waiting for warehouse fulfillment");
    }
}
```

**Co się stało:**
1. ✅ Anna skonfigurowała pakiet wybierając **TYPY** produktów
2. ✅ System zwalidował jej wybór względem SelectionRules pakietu
3. ✅ Utworzono zamówienie (Order) z jej wyborami
4. ❌ **Nie ma jeszcze PackageInstance** - to dopiero nastąpi w magazynie!

**Kluczowa różnica:**
- `SelectedProduct(ProductIdentifier)` - "chcę smartfona typu Standard"
- `SelectedInstance(Instance)` - "dostajesz smartfona o seryjnym nr SN-12345"

---

## Krok 3: Magazyn realizuje zamówienie (Fulfillment)

**15 grudnia 2025, godz. 16:45**
Zamówienie Anny trafia do magazynu. Pracownik magazynu Michał musi **przydzielić konkretne egzemplarze** produktów.

```java
public class WarehouseFulfillment {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final PackageInstanceRepository packageInstanceRepository;

    public void fulfillAnnaOrder() {
        // ========== POBRANIE ZAMÓWIENIA ==========

        Order annaOrder = orderRepository.findById(
            OrderId.of("ORD-2025-12-15-0001423")
        );

        System.out.println("=== Fulfilling order for: " + annaOrder.customerId() + " ===");
        System.out.println("Package: " + annaOrder.packageType().name());
        System.out.println("Selected products (types): " + annaOrder.selectedProducts().size());

        // ========== PRZYDZIELANIE KONKRETNYCH EGZEMPLARZY ==========

        // Anna wybrała TYPY produktów.
        // Teraz magazyn musi przydzielić KONKRETNE EGZEMPLARZE.

        List<SelectedInstance> allocatedInstances = new ArrayList<>();

        // --- 1. SMARTFON: Standard 5G ---
        // Anna wybrała TYP: PHONE-STD-001
        // Magazyn znajduje dostępny egzemplarz tego typu

        ProductType phoneStandardType = productRepository.findById(
            ProductIdentifier.of("PHONE-STD-001")
        );

        // Znajdź dostępne egzemplarze w magazynie
        List<ProductInstance> availablePhones = inventoryService
            .findAvailableInstances(phoneStandardType.id());

        if (availablePhones.isEmpty()) {
            throw new OutOfStockException("SmartPhone Standard 5G not available");
        }

        // Weź pierwszy dostępny egzemplarz
        ProductInstance allocatedPhone = availablePhones.get(0);

        System.out.println("Allocated phone: " + allocatedPhone.id() +
                          " (serial: " + allocatedPhone.serialNumber().orElse("none") + ")");

        // Dodaj do listy przydzielonych egzemplarzy
        allocatedInstances.add(new SelectedInstance(allocatedPhone, 1));

        // Zarezerwuj w magazynie (już nie jest dostępny dla innych)
        inventoryService.reserve(allocatedPhone.id());

        // --- 2. KARTA SIM: 5G SIM Card ---
        // Anna wybrała TYP: SIM-5G-001
        // Karta SIM ma unikalny IMSI i przydzielony numer telefonu

        ProductType simCardType = productRepository.findById(
            ProductIdentifier.of("SIM-5G-001")
        );

        List<ProductInstance> availableSIMs = inventoryService
            .findAvailableInstances(simCardType.id());

        if (availableSIMs.isEmpty()) {
            throw new OutOfStockException("5G SIM Card not available");
        }

        ProductInstance allocatedSIM = availableSIMs.get(0);

        // Karta SIM ma features: IMSI i przydzielony numer
        String imsi = allocatedSIM.features()
            .getValue(ProductFeatureType.of("IMSI"))
            .orElseThrow()
            .toString();

        String phoneNumber = allocatedSIM.features()
            .getValue(ProductFeatureType.of("phoneNumber"))
            .orElseThrow()
            .toString();

        System.out.println("Allocated SIM: " + allocatedSIM.id() +
                          " (IMSI: " + imsi + ", number: " + phoneNumber + ")");

        allocatedInstances.add(new SelectedInstance(allocatedSIM, 1));
        inventoryService.reserve(allocatedSIM.id());

        // --- 3. PLAN TARYFOWY: Premium Plan ---
        // Plan nie ma fizycznego egzemplarza - to usługa
        // Ale tworzymy ProductInstance żeby śledzić subskrypcję

        ProductType planPremiumType = productRepository.findById(
            ProductIdentifier.of("PLAN-PREM-001")
        );

        // Tworzymy nową instancję planu dla Anny
        ProductInstance annaPlanInstance = new InstanceBuilder(InstanceId.newOne())
            .withBatch(BatchId.of("PLAN-BATCH-2025-12"))  // plany w batchu miesięcznym
            .asProductInstance(planPremiumType)
                .build();

        System.out.println("Created plan instance: " + annaPlanInstance.id() +
                          " (batch: PLAN-BATCH-2025-12)");

        allocatedInstances.add(new SelectedInstance(annaPlanInstance, 1));

        // --- 4. USŁUGI DODATKOWE: Roaming + Family Share ---
        // Podobnie jak plany - to usługi, nie fizyczne produkty

        ProductType roamingType = productRepository.findById(
            ProductIdentifier.of("SVC-ROAM-001")
        );

        ProductInstance annaRoamingInstance = new InstanceBuilder(InstanceId.newOne())
            .withBatch(BatchId.of("SVC-BATCH-2025-12"))
            .asProductInstance(roamingType)
                .build();

        allocatedInstances.add(new SelectedInstance(annaRoamingInstance, 1));

        ProductType familyShareType = productRepository.findById(
            ProductIdentifier.of("SVC-FAMILY-001")
        );

        ProductInstance annaFamilyShareInstance = new InstanceBuilder(InstanceId.newOne())
            .withBatch(BatchId.of("SVC-BATCH-2025-12"))
            .asProductInstance(familyShareType)
                .build();

        allocatedInstances.add(new SelectedInstance(annaFamilyShareInstance, 1));

        System.out.println("Created service instances: Roaming + Family Share");

        // ========== TWORZENIE PACKAGEINSTANCE ==========

        // Teraz mamy wszystkie konkretne egzemplarze.
        // Tworzymy PackageInstance - to co Anna FAKTYCZNIE dostanie.

        PackageInstance annaPackageInstance = new InstanceBuilder(InstanceId.newOne())
            .withSerial(SerialNumber.of("PKG-2025-12-15-0001423"))  // unikalny serial pakietu
            .asPackageInstance(annaOrder.packageType())
                .withSelection(allocatedInstances)  // ← KONKRETNE egzemplarze!
                .build();

        // ========== WALIDACJA W KONSTRUKTORZE ==========

        // W konstruktorze PackageInstance automatycznie:
        // 1. Konwertuje List<SelectedInstance> na List<SelectedProduct>
        //    (wywołuje toSelectedProduct() na każdym elemencie)
        // 2. Waliduje czy TYPY produktów spełniają SelectionRules pakietu
        // 3. Rzuca wyjątek jeśli coś jest nie tak

        // W tym przypadku:
        // - allocatedInstances zawiera: phone(Standard), SIM, plan(Premium), roaming, familyShare
        // - Po konwersji: PHONE-STD-001, SIM-5G-001, PLAN-PREM-001, SVC-ROAM-001, SVC-FAMILY-001
        // - Walidacja sprawdza: ✓ 1 phone, ✓ 1 SIM, ✓ 1 plan, ✓ 2 services, ✓ reguły biznesowe OK

        System.out.println("\n=== PackageInstance created successfully! ===");
        System.out.println("PackageInstance ID: " + annaPackageInstance.id());
        System.out.println("Serial: " + annaPackageInstance.serialNumber().orElse("none"));
        System.out.println("Delivered instances: " + annaPackageInstance.selection().size());

        // Wypisz szczegóły każdego egzemplarza
        for (SelectedInstance si : annaPackageInstance.selection()) {
            Instance instance = si.instance();
            System.out.println(
                "  - " + instance.product().name() +
                " (id: " + instance.id() +
                ", serial: " + instance.serialNumber().orElse("batch: " + instance.batchId().orElse("none")) + ")"
            );
        }

        // ========== PERSYSTENCJA I FINALIZACJA ==========

        // Zapisz PackageInstance
        packageInstanceRepository.save(annaPackageInstance);

        // Zaktualizuj zamówienie
        annaOrder.markAsFulfilled(annaPackageInstance.id());
        annaOrder.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(annaOrder);

        System.out.println("\nOrder status: SHIPPED");
        System.out.println("Package ready for delivery!");
    }
}

// Output programu:
//
// === Fulfilling order for: CUST-ANNA-KOWALSKA ===
// Package: 5G Starter Pack
// Selected products (types): 5
// Allocated phone: <UUID> (serial: SN-PHONE-STD-784521)
// Allocated SIM: <UUID> (IMSI: 260011234567890, number: +48 600 123 456)
// Created plan instance: <UUID> (batch: PLAN-BATCH-2025-12)
// Created service instances: Roaming + Family Share
//
// === PackageInstance created successfully! ===
// PackageInstance ID: <UUID>
// Serial: PKG-2025-12-15-0001423
// Delivered instances: 5
//   - SmartPhone Standard 5G (id: <UUID>, serial: SN-PHONE-STD-784521)
//   - 5G SIM Card (id: <UUID>, serial: SN-SIM-456123)
//   - 5G Premium Plan (id: <UUID>, batch: PLAN-BATCH-2025-12)
//   - International Roaming (id: <UUID>, batch: SVC-BATCH-2025-12)
//   - Family Share (id: <UUID>, batch: SVC-BATCH-2025-12)
//
// Order status: SHIPPED
// Package ready for delivery!
```

**Co się stało w magazynie:**

1. ✅ **Pobrano zamówienie** - wiemy CO Anna wybrała (typy)
2. ✅ **Przydzielono egzemplarze** - dla każdego typu znaleziono konkretny egzemplarz:
   - Smartfon: serial SN-PHONE-STD-784521
   - SIM: IMSI 260011234567890, numer +48 600 123 456
   - Plan: utworzono instancję subskrypcji (batch PLAN-BATCH-2025-12)
   - Usługi: utworzono instancje usług
3. ✅ **Utworzono PackageInstance** - z `List<SelectedInstance>` (konkretne egzemplarze!)
4. ✅ **Automatyczna walidacja** - konstruktor sprawdził czy typy spełniają reguły pakietu
5. ✅ **Persystencja** - zapisano PackageInstance i zaktualizowano Order

**Kluczowa obserwacja:**
```java
// W Order (zamówienie):
List<SelectedProduct> selection;  // TYPY: "chcę Standard phone, Premium plan"

// W PackageInstance (dostawa):
List<SelectedInstance> selection;  // EGZEMPLARZE: "dostajesz phone SN-784521, SIM z numerem +48 600..."
```

---

## Krok 4: Dostawa i życie pakietu

**16 grudnia 2025, godz. 10:00**
Anna odbiera swoją przesyłkę. W pudełku znajduje:
- Smartfon z serialem SN-PHONE-STD-784521
- Kartę SIM z numerem +48 600 123 456
- Umowę na Plan Premium + Roaming + Family Share

```java
public class PackageLifecycle {

    public void annaUsesHerPackage() {
        // Anna ma teraz PackageInstance
        PackageInstance annaPackage = packageInstanceRepository.findById(
            InstanceId.of("...")
        );

        // ========== ZAPYTANIA BIZNESOWE ==========

        // 1. Jaki numer telefonu ma Anna?
        Instance simInstance = annaPackage.selection().stream()
            .map(SelectedInstance::instance)
            .filter(i -> i.product().name().value().contains("SIM"))
            .findFirst()
            .orElseThrow();

        if (simInstance instanceof ProductInstance productInstance) {
            String phoneNumber = productInstance.features()
                .getValue(ProductFeatureType.of("phoneNumber"))
                .orElseThrow()
                .toString();

            System.out.println("Anna's phone number: " + phoneNumber);
            // Output: Anna's phone number: +48 600 123 456
        }

        // 2. Jaki plan ma Anna? (do rozliczeń)
        Product annaPlan = annaPackage.selection().stream()
            .map(si -> si.instance().product())
            .filter(p -> p.metadata().get("category").equals("plan"))
            .findFirst()
            .orElseThrow();

        System.out.println("Anna's plan: " + annaPlan.name());
        // Output: Anna's plan: 5G Premium Plan

        // 3. Jakie usługi dodatkowe ma aktywne?
        List<Product> annaServices = annaPackage.selection().stream()
            .map(si -> si.instance().product())
            .filter(p -> p.metadata().get("category").equals("service"))
            .toList();

        System.out.println("Anna's services: " + annaServices.size());
        for (Product service : annaServices) {
            System.out.println("  - " + service.name());
        }
        // Output:
        // Anna's services: 2
        //   - International Roaming
        //   - Family Share

        // ========== UPGRADE SCENARIO ==========

        // Anna chce upgrade'ować do Unlimited Plan (6 miesięcy później)
        // Opcja 1: Tworzymy nowy PackageInstance z nowym planem
        // Opcja 2: Modyfikujemy istniejący (wymiana instance planu)

        // Sprawdź czy może upgrade'ować (relacje produktów!)
        ProductRelationship upgradeRelation = relationshipRepository
            .findByFromAndType(
                annaPlan.id(),
                ProductRelationshipType.UPGRADABLE_TO
            )
            .stream()
            .findFirst()
            .orElseThrow(() -> new UpgradeNotAvailableException("No upgrade path"));

        System.out.println("Available upgrade: " + upgradeRelation.to());
        // Upgrade z Premium Plan → Unlimited Plan

        // ========== WARRANTY CLAIM ==========

        // Smartfon Anny się zepsuł (po 2 miesiącach)
        // Musimy wymienić konkretny egzemplarz w pakiecie

        SelectedInstance phoneInstance = annaPackage.selection().stream()
            .filter(si -> si.product().metadata().get("category").equals("smartphone"))
            .findFirst()
            .orElseThrow();

        System.out.println("Defective phone: " + phoneInstance.instanceId());
        System.out.println("Original serial: " + phoneInstance.instance().serialNumber().orElse("none"));

        // Znajdź replacement (ten sam TYP!)
        ProductIdentifier phoneTypeId = phoneInstance.productId();
        List<ProductInstance> replacements = inventoryService
            .findAvailableInstances(phoneTypeId);

        if (replacements.isEmpty()) {
            throw new ReplacementNotAvailableException("No replacement phones available");
        }

        ProductInstance replacementPhone = replacements.get(0);

        // Wymień egzemplarz w pakiecie
        // (wymaga stworzenia nowego PackageInstance lub mutacji - zależy od modelu)
        System.out.println("Replacement phone: " + replacementPhone.id());
        System.out.println("New serial: " + replacementPhone.serialNumber().orElse("none"));
    }
}
```

---

## Kluczowe takeaways

### 1. Dwa światy: Types vs Instances

```
ORDER (wybór)                    DELIVERY (realizacja)
┌──────────────────────┐        ┌───────────────────────┐
│ SelectedProduct      │        │ SelectedInstance      │
│                      │        │                       │
│ ProductIdentifier    │   →    │ Instance              │
│ "chcę Standard"      │        │ "dostajesz SN-12345"  │
│                      │        │                       │
│ Walidacja: czy typ   │        │ Walidacja: czy typ    │
│ spełnia reguły?      │        │ egzemplarza == typ    │
│                      │        │ zamówienia?           │
└──────────────────────┘        └───────────────────────┘
```

### 2. PackageInstance = Proof of Delivery

PackageInstance to **dowód dostawy**:
- Wiemy KTO dostał (Order.customerId)
- Wiemy CO dostał (PackageType)
- Wiemy KTÓRE EGZEMPLARZE dostał (List<SelectedInstance>)
- Możemy trackować każdy element (serial numbers, IMSI, batch)

### 3. Automatyczna walidacja

```java
PackageInstance(id, packageType, selection, serial, batch) {
    // ...
    validateSelection(packageType, selection);  // ← automatycznie!
    // ...
}

private static void validateSelection(PackageType packageType, List<SelectedInstance> selection) {
    List<SelectedProduct> selectedProducts = selection.stream()
        .map(SelectedInstance::toSelectedProduct)  // Instance → ProductId
        .toList();

    PackageValidationResult result = packageType.validateSelection(selectedProducts);

    if (!result.isValid()) {
        throw new IllegalArgumentException("Invalid selection: " + result.errors());
    }
}
```

**Dlaczego to jest potężne:**
- Nie możesz stworzyć PackageInstance z niewłaściwymi produktami
- Walidacja dzieje się w konstruktorze - fail-fast
- Reguły biznesowe (SelectionRules) są egzekwowane zawsze

### 4. SelectedInstance.toSelectedProduct() - most między światami

```java
// SelectedInstance: "mam konkretny egzemplarz smartfona serial SN-12345"
SelectedInstance si = new SelectedInstance(phoneInstance, 1);

// toSelectedProduct(): "ten egzemplarz jest typu PHONE-STD-001"
SelectedProduct sp = si.toSelectedProduct();

// Używamy do walidacji reguł (które operują na TYPACH, nie egzemplarzach)
packageType.validateSelection(List.of(sp));
```

### 5. Lifecycle: Order → Fulfillment → PackageInstance → Business Operations

1. **Order** - klient wybiera typy, system waliduje, zapisuje zamówienie
2. **Fulfillment** - magazyn przydziela egzemplarze, tworzy PackageInstance
3. **Delivery** - PackageInstance to dowód co klient dostał
4. **Operations** - upgrade'y, warranty, billing - wszystko oparte na PackageInstance

---

## Podsumowanie

**PackageInstance** to nie tylko "lista produktów" - to **dowód transakcji** z pełną trace-ability:

✅ **CO klient wybrał** - typy produktów (można odtworzyć z instances)
✅ **CO klient dostał** - konkretne egzemplarze z serialami/IMSI/batch
✅ **KIEDY** - serial number pakietu, timestamp
✅ **Walidacja** - automatyczna w konstruktorze, fail-fast
✅ **Trackability** - każdy element identyfikowalny
✅ **Business operations** - upgrade, warranty, billing

Model jest **kompletny**: od wyboru przez realizację aż po życie pakietu w produkcji.
