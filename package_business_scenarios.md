# Scenariusze biznesowe pakietów - przykłady z kodem

## Scenariusz 1: Banking Package z regułami AND/OR i NOT

### Biznesowy opis

Bank oferuje **Premium Banking Package** z następującymi regułami:

**Produkty dostępne:**
- **Konta:** Personal Basic, Personal Premium, Business Standard, Business Premium
- **Karty:** Debit Standard, Debit Gold, Credit Standard, Credit Gold, Business Card
- **Usługi:** Mobile App, SMS Notifications, Insurance Basic, Insurance Extended
- **Kanały:** Branch Only, Branch + Online, Branch + Online + Mobile

**Reguły biznesowe:**
1. Musisz wybrać dokładnie 1 konto
2. Musisz wybrać dokładnie 1 kartę
3. Musisz wybrać przynajmniej 1 kanał dostępu
4. **IF konto Business** → THEN karta MUSI być Business Card
5. **IF konto Premium (Personal Premium OR Business Premium)** → THEN:
   - Insurance MUSI być Extended (nie może być Basic)
   - Kanał MUSI zawierać Mobile (Branch + Online + Mobile)
6. Możesz wybrać 0-2 dodatkowe usługi (Mobile App, SMS)

### Kod implementacji

```java
package com.softwarearchetypes.product.scenarios;

import com.softwarearchetypes.product.*;

public class BankingPackageScenario {

    // ===== KROK 1: Definiujemy wszystkie ProductType =====

    private final ProductType personalBasic = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Personal Basic Account"),
        ProductDescription.of("Basic personal banking account")
    )
        .withMetadata("category", "account")
        .withMetadata("accountType", "personal")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType personalPremium = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Personal Premium Account"),
        ProductDescription.of("Premium personal banking with benefits")
    )
        .withMetadata("category", "account")
        .withMetadata("accountType", "personal")
        .withMetadata("tier", "premium")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType businessStandard = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Business Standard Account"),
        ProductDescription.of("Standard business banking account")
    )
        .withMetadata("category", "account")
        .withMetadata("accountType", "business")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType businessPremium = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Business Premium Account"),
        ProductDescription.of("Premium business banking with priority service")
    )
        .withMetadata("category", "account")
        .withMetadata("accountType", "business")
        .withMetadata("tier", "premium")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // Karty
    private final ProductType debitStandard = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Debit Card Standard"),
        ProductDescription.of("Standard debit card")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    private final ProductType debitGold = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Debit Card Gold"),
        ProductDescription.of("Gold debit card with cashback")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    private final ProductType businessCard = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Business Card"),
        ProductDescription.of("Business expense card with reporting")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    // Ubezpieczenia
    private final ProductType insuranceBasic = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Insurance Basic"),
        ProductDescription.of("Basic account insurance")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType insuranceExtended = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Insurance Extended"),
        ProductDescription.of("Extended account insurance with travel cover")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // Kanały dostępu
    private final ProductType channelBranch = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Branch Only Access"),
        ProductDescription.of("Access via bank branches only")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType channelBranchOnline = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Branch + Online Access"),
        ProductDescription.of("Access via branches and online banking")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType channelFull = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Branch + Online + Mobile"),
        ProductDescription.of("Full access via all channels including mobile app")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // Usługi dodatkowe
    private final ProductType mobileApp = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Mobile App Access"),
        ProductDescription.of("Premium mobile banking app")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType smsNotifications = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("SMS Notifications"),
        ProductDescription.of("Transaction SMS alerts")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // ===== KROK 2: Tworzymy ProductSets (grupy produktów) =====

    // Wszystkie konta biznesowe
    private final ProductSet businessAccounts = new ProductSet("BusinessAccounts",
        Set.of(businessStandard.id(), businessPremium.id()));

    // Wszystkie konta premium (personal OR business)
    private final ProductSet premiumAccounts = new ProductSet("PremiumAccounts",
        Set.of(personalPremium.id(), businessPremium.id()));

    // Kanały z Mobile
    private final ProductSet mobileChannels = new ProductSet("MobileChannels",
        Set.of(channelFull.id()));

    // Business Card jako set
    private final ProductSet businessCards = new ProductSet("BusinessCards",
        Set.of(businessCard.id()));

    // Extended Insurance jako set
    private final ProductSet extendedInsurance = new ProductSet("ExtendedInsurance",
        Set.of(insuranceExtended.id()));

    // Basic Insurance jako set (dla negacji)
    private final ProductSet basicInsurance = new ProductSet("BasicInsurance",
        Set.of(insuranceBasic.id()));

    // ===== KROK 3: Budujemy reguły biznesowe =====

    // Reguła 1: IF Business Account THEN Business Card
    private final SelectionRule businessAccountRule = SelectionRule.ifThen(
        SelectionRule.required(businessAccounts),  // condition: wybrał business account
        SelectionRule.single(businessCards)        // then: MUSI wybrać business card
    );

    // Reguła 2: IF Premium Account THEN (Extended Insurance AND Mobile Channel)
    private final SelectionRule premiumAccountRule = SelectionRule.ifThen(
        SelectionRule.required(premiumAccounts),   // condition: wybrał premium account
        // then: MUSI spełnić OBA warunki (AND)
        SelectionRule.and(
            SelectionRule.single(extendedInsurance),    // musi mieć extended insurance
            SelectionRule.not(SelectionRule.required(basicInsurance)),  // NIE MOŻE mieć basic
            SelectionRule.single(mobileChannels)        // musi mieć mobile channel
        )
    );

    // ===== KROK 4: Budujemy PackageType z fluent API =====

    public PackageType createPremiumBankingPackage() {
        return new ProductBuilder(
            ProductIdentifier.newOne(),
            ProductName.of("Premium Banking Package"),
            ProductDescription.of("Complete banking package with account, card, and services")
        )
            .withMetadata("category", "banking")
            .withMetadata("targetSegment", "premium-customers")
            .withApplicabilityConstraint(
                ApplicabilityConstraint.greaterThan("monthlyIncome", 5000)
            )
            .asPackage()
                // Wybór konta: dokładnie 1
                .withSingleChoice("Account",
                    personalBasic.id(),
                    personalPremium.id(),
                    businessStandard.id(),
                    businessPremium.id()
                )
                // Wybór karty: dokładnie 1
                .withSingleChoice("Card",
                    debitStandard.id(),
                    debitGold.id(),
                    businessCard.id()
                )
                // Wybór ubezpieczenia: dokładnie 1
                .withSingleChoice("Insurance",
                    insuranceBasic.id(),
                    insuranceExtended.id()
                )
                // Wybór kanału: dokładnie 1
                .withSingleChoice("Channel",
                    channelBranch.id(),
                    channelBranchOnline.id(),
                    channelFull.id()
                )
                // Usługi dodatkowe: 0-2
                .withChoice("AdditionalServices", 0, 2,
                    mobileApp.id(),
                    smsNotifications.id()
                )
                // Dodajemy zaawansowane reguły biznesowe
                .withSelectionRule(businessAccountRule)
                .withSelectionRule(premiumAccountRule)
                .build();
    }

    // ===== KROK 5: Scenariusze użycia =====

    public void testScenario_PersonalPremium_Valid() {
        PackageType bankingPackage = createPremiumBankingPackage();

        // Klient wybiera:
        // - Personal Premium Account (premium!)
        // - Debit Gold Card
        // - Extended Insurance (wymagane dla premium!)
        // - Full Channel with Mobile (wymagane dla premium!)
        // - Mobile App + SMS (2 usługi dodatkowe)

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(personalPremium.id(), 1),
            new SelectedProduct(debitGold.id(), 1),
            new SelectedProduct(insuranceExtended.id(), 1),
            new SelectedProduct(channelFull.id(), 1),
            new SelectedProduct(mobileApp.id(), 1),
            new SelectedProduct(smsNotifications.id(), 1)
        );

        PackageValidationResult result = bankingPackage.validateSelection(selection);

        // ✅ PASS: wszystkie reguły spełnione
        // - Premium account → Extended insurance ✓
        // - Premium account → Mobile channel ✓
        // - Premium account → NOT Basic insurance ✓
        assert result.isValid();
    }

    public void testScenario_PersonalPremium_Invalid_BasicInsurance() {
        PackageType bankingPackage = createPremiumBankingPackage();

        // Klient wybiera:
        // - Personal Premium Account (premium!)
        // - Debit Gold Card
        // - Basic Insurance (❌ błąd! Premium wymaga Extended!)
        // - Full Channel with Mobile

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(personalPremium.id(), 1),
            new SelectedProduct(debitGold.id(), 1),
            new SelectedProduct(insuranceBasic.id(), 1),  // ❌
            new SelectedProduct(channelFull.id(), 1)
        );

        PackageValidationResult result = bankingPackage.validateSelection(selection);

        // ❌ FAIL: Premium account wymaga Extended Insurance i NIE MOŻE mieć Basic
        assert !result.isValid();
        assert result.errors().contains("Rule not satisfied: IF(...) THEN(...)");
    }

    public void testScenario_Business_Invalid_WrongCard() {
        PackageType bankingPackage = createPremiumBankingPackage();

        // Klient wybiera:
        // - Business Standard Account (business!)
        // - Debit Standard Card (❌ błąd! Business wymaga Business Card!)
        // - Basic Insurance
        // - Branch only

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(businessStandard.id(), 1),
            new SelectedProduct(debitStandard.id(), 1),  // ❌
            new SelectedProduct(insuranceBasic.id(), 1),
            new SelectedProduct(channelBranch.id(), 1)
        );

        PackageValidationResult result = bankingPackage.validateSelection(selection);

        // ❌ FAIL: Business account wymaga Business Card
        assert !result.isValid();
    }

    public void testScenario_PersonalBasic_Valid() {
        PackageType bankingPackage = createPremiumBankingPackage();

        // Klient wybiera:
        // - Personal Basic Account (nie premium, nie business)
        // - Debit Standard Card (dowolna karta OK)
        // - Basic Insurance (OK bo to nie premium account)
        // - Branch only (OK bo to nie premium account)

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(personalBasic.id(), 1),
            new SelectedProduct(debitStandard.id(), 1),
            new SelectedProduct(insuranceBasic.id(), 1),
            new SelectedProduct(channelBranch.id(), 1)
        );

        PackageValidationResult result = bankingPackage.validateSelection(selection);

        // ✅ PASS: Personal Basic nie ma dodatkowych ograniczeń
        // Warunki IF nie są spełnione, więc reguły THEN się nie aktywują
        assert result.isValid();
    }

    // ===== KROK 6: Tworzenie instancji pakietu (co klient faktycznie kupił) =====

    public void createPackageInstance() {
        PackageType bankingPackage = createPremiumBankingPackage();

        // Klient Jan Kowalski wybrał:
        List<SelectedProduct> janSelection = List.of(
            new SelectedProduct(personalPremium.id(), 1),
            new SelectedProduct(debitGold.id(), 1),
            new SelectedProduct(insuranceExtended.id(), 1),
            new SelectedProduct(channelFull.id(), 1),
            new SelectedProduct(mobileApp.id(), 1)
        );

        // Tworzymy instancję pakietu używając InstanceBuilder
        PackageInstance janPackage = new InstanceBuilder(InstanceId.newOne())
            .withSerial(SerialNumber.of("PKG-JAN-2025-001"))
            .asPackageInstance(bankingPackage)
                .withSelection(janSelection)
                .build();

        // ✅ PackageInstance zawiera:
        // - Unikalny InstanceId
        // - Referencję do PackageType (Premium Banking Package)
        // - Konkretne wybory klienta (Personal Premium + Debit Gold + ...)
        // - Serial number dla trackingu
    }
}
```

### Wyjaśnienie co się dzieje w kodzie

**Krok 1-2:** Definiujemy wszystkie produkty i grupujemy je w ProductSets
- ProductSet `businessAccounts` = {Business Standard, Business Premium}
- ProductSet `premiumAccounts` = {Personal Premium, Business Premium}

**Krok 3:** Budujemy zaawansowane reguły:

```java
// Reguła: IF Business Account THEN Business Card
businessAccountRule = IF (selected from businessAccounts)
                      THEN (must select from businessCards)

// Reguła: IF Premium Account THEN (Extended Insurance AND Mobile Channel AND NOT Basic)
premiumAccountRule = IF (selected from premiumAccounts)
                     THEN (
                         must select extendedInsurance
                         AND must NOT select basicInsurance
                         AND must select mobileChannels
                     )
```

**Krok 4:** Fluent API buduje PackageType:
- `withSingleChoice()` - dokładnie 1 produkt z grupy
- `withChoice(0, 2, ...)` - 0-2 produkty z grupy
- `withSelectionRule()` - dodaje zaawansowane reguły biznesowe

**Krok 5:** Testujemy scenariusze:
1. ✅ Premium + Extended + Mobile = OK
2. ❌ Premium + Basic Insurance = FAIL (NOT rule)
3. ❌ Business + Debit Card = FAIL (wymaga Business Card)
4. ✅ Basic + Basic Insurance = OK (warunki IF nie spełnione)

---

## Scenariusz 2: Logistics Package z zagnieżdżonymi paczkami

### Biznesowy opis

Firma logistyczna oferuje **Complete Shipping Solution** - megapakiet składający się z mniejszych pakietów:

**Struktura:**
```
Complete Shipping Solution (główny pakiet)
├── Transport Package (pakiet)
│   ├── Domestic Transport (produkt)
│   ├── International Transport (produkt)
│   └── Express Transport (produkt)
├── Insurance Package (pakiet)
│   ├── Basic Coverage (produkt)
│   ├── Extended Coverage (produkt)
│   └── Premium Coverage (produkt)
└── Notifications Package (pakiet)
    ├── Email Notifications (produkt)
    ├── SMS Notifications (produkt)
    └── Mobile App Push (produkt)
```

**Reguły biznesowe:**
1. Musisz wybrać dokładnie 1 Transport Package
2. Musisz wybrać dokładnie 1 Insurance Package
3. Możesz wybrać 0-1 Notifications Package (opcjonalnie)
4. **IF International Transport** → THEN Insurance MUSI być Extended LUB Premium (nie może być Basic)
5. **IF Express Transport** → THEN Notifications Package jest WYMAGANY

### Kod implementacji

```java
package com.softwarearchetypes.product.scenarios;

import com.softwarearchetypes.product.*;

public class LogisticsNestedPackageScenario {

    // ===== KROK 1: Produkty transportowe =====

    private final ProductType domesticTransport = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Domestic Transport 24h"),
        ProductDescription.of("Standard domestic shipping within 24h")
    )
        .withMetadata("transportType", "domestic")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.BATCH)
            .build();

    private final ProductType internationalTransport = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("International Transport"),
        ProductDescription.of("International shipping with customs clearance")
    )
        .withMetadata("transportType", "international")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.BATCH)
            .build();

    private final ProductType expressTransport = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Express Transport 4h"),
        ProductDescription.of("Same-day express delivery within 4 hours")
    )
        .withMetadata("transportType", "express")
        .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    // ===== KROK 2: Produkty ubezpieczeniowe =====

    private final ProductType basicCoverage = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Basic Insurance Coverage"),
        ProductDescription.of("Basic liability coverage up to 1000 EUR")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType extendedCoverage = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Extended Insurance Coverage"),
        ProductDescription.of("Extended coverage up to 10000 EUR")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType premiumCoverage = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Premium Insurance Coverage"),
        ProductDescription.of("Premium all-risk coverage up to 50000 EUR")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // ===== KROK 3: Produkty powiadomień =====

    private final ProductType emailNotifications = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Email Notifications"),
        ProductDescription.of("Shipment status via email")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType smsNotifications = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("SMS Notifications"),
        ProductDescription.of("Real-time SMS alerts")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    private final ProductType pushNotifications = new ProductBuilder(
        ProductIdentifier.newOne(),
        ProductName.of("Mobile Push Notifications"),
        ProductDescription.of("Mobile app push notifications with live tracking")
    )
        .asProductType(Unit.pieces(), ProductTrackingStrategy.UNIQUE)
            .build();

    // ===== KROK 4: Budujemy ZAGNIEŻDŻONE pakiety (packages w package) =====

    // Pakiet 1: Transport Package (zawiera produkty transportowe)
    private PackageType createTransportPackage() {
        return new ProductBuilder(
            ProductIdentifier.newOne(),
            ProductName.of("Transport Package"),
            ProductDescription.of("Choose your transport method")
        )
            .withMetadata("packageType", "transport")
            .asPackage()
                // Wybierz dokładnie 1 rodzaj transportu
                .withSingleChoice("TransportMethod",
                    domesticTransport.id(),
                    internationalTransport.id(),
                    expressTransport.id()
                )
                .build();
    }

    // Pakiet 2: Insurance Package (zawiera produkty ubezpieczeniowe)
    private PackageType createInsurancePackage() {
        return new ProductBuilder(
            ProductIdentifier.newOne(),
            ProductName.of("Insurance Package"),
            ProductDescription.of("Choose your insurance level")
        )
            .withMetadata("packageType", "insurance")
            .asPackage()
                // Wybierz dokładnie 1 poziom ubezpieczenia
                .withSingleChoice("InsuranceLevel",
                    basicCoverage.id(),
                    extendedCoverage.id(),
                    premiumCoverage.id()
                )
                .build();
    }

    // Pakiet 3: Notifications Package (zawiera produkty powiadomień)
    private PackageType createNotificationsPackage() {
        return new ProductBuilder(
            ProductIdentifier.newOne(),
            ProductName.of("Notifications Package"),
            ProductDescription.of("Choose your notification channels")
        )
            .withMetadata("packageType", "notifications")
            .asPackage()
                // Wybierz 1-3 kanały powiadomień
                .withChoice("NotificationChannels", 1, 3,
                    emailNotifications.id(),
                    smsNotifications.id(),
                    pushNotifications.id()
                )
                .build();
    }

    // ===== KROK 5: Główny pakiet zawierający inne PAKIETY =====

    public PackageType createCompleteShippingSolution() {
        // Najpierw tworzymy sub-packages
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();
        PackageType notificationsPackage = createNotificationsPackage();

        // ProductSets dla reguł biznesowych
        // International transport (musimy stworzyć set z produktem, nie z pakietem)
        ProductSet internationalTransportSet = new ProductSet("InternationalTransport",
            Set.of(internationalTransport.id()));

        // Express transport
        ProductSet expressTransportSet = new ProductSet("ExpressTransport",
            Set.of(expressTransport.id()));

        // Advanced insurance (Extended OR Premium)
        ProductSet advancedInsurance = new ProductSet("AdvancedInsurance",
            Set.of(extendedCoverage.id(), premiumCoverage.id()));

        // Basic insurance (dla negacji)
        ProductSet basicInsuranceSet = new ProductSet("BasicInsurance",
            Set.of(basicCoverage.id()));

        // Notifications package jako set
        ProductSet notificationsSet = new ProductSet("NotificationsSet",
            Set.of(notificationsPackage.id()));  // ← To jest PackageType.id()!

        // Reguła 1: IF International Transport THEN (Extended OR Premium Insurance)
        SelectionRule internationalInsuranceRule = SelectionRule.ifThen(
            SelectionRule.required(internationalTransportSet),
            SelectionRule.and(
                SelectionRule.or(  // Extended OR Premium
                    SelectionRule.required(advancedInsurance)
                ),
                SelectionRule.not(SelectionRule.required(basicInsuranceSet))  // NOT Basic
            )
        );

        // Reguła 2: IF Express Transport THEN Notifications Package REQUIRED
        SelectionRule expressNotificationsRule = SelectionRule.ifThen(
            SelectionRule.required(expressTransportSet),
            SelectionRule.single(notificationsSet)  // MUSI wybrać notifications package
        );

        // Budujemy główny pakiet zawierający INNE PAKIETY
        return new ProductBuilder(
            ProductIdentifier.newOne(),
            ProductName.of("Complete Shipping Solution"),
            ProductDescription.of("All-in-one shipping package with transport, insurance, and notifications")
        )
            .withMetadata("category", "logistics")
            .withMetadata("packageLevel", "composite")  // to jest pakiet pakietów!
            .asPackage()
                // Wybór Transport Package: dokładnie 1
                // ← To jest PAKIET, nie produkt!
                .withSingleChoice("Transport", transportPackage.id())

                // Wybór Insurance Package: dokładnie 1
                // ← To jest PAKIET, nie produkt!
                .withSingleChoice("Insurance", insurancePackage.id())

                // Wybór Notifications Package: opcjonalnie 0-1
                // ← To jest PAKIET, nie produkt!
                .withOptionalChoice("Notifications", notificationsPackage.id())

                // Zaawansowane reguły biznesowe
                .withSelectionRule(internationalInsuranceRule)
                .withSelectionRule(expressNotificationsRule)
                .build();
    }

    // ===== KROK 6: Scenariusze użycia zagnieżdżonych pakietów =====

    public void testScenario_International_Valid() {
        PackageType shippingSolution = createCompleteShippingSolution();
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();
        PackageType notificationsPackage = createNotificationsPackage();

        // Klient wybiera:
        // Na poziomie głównego pakietu:
        // - Transport Package
        // - Insurance Package
        // - Notifications Package (opcjonalnie)
        //
        // Ale wewnątrz tych pakietów też musi wybrać konkretne produkty!

        // Wybory na poziomie GŁÓWNEGO pakietu
        List<SelectedProduct> mainSelection = List.of(
            new SelectedProduct(transportPackage.id(), 1),      // wybrał pakiet transportowy
            new SelectedProduct(insurancePackage.id(), 1),      // wybrał pakiet ubezpieczeniowy
            new SelectedProduct(notificationsPackage.id(), 1)   // wybrał pakiet powiadomień
        );

        // ALE! Żeby walidacja sprawdziła nasze reguły (IF International THEN Extended),
        // musimy przekazać WSZYSTKIE wybory włącznie z tym CO JEST W ŚRODKU pakietów
        List<SelectedProduct> fullSelection = List.of(
            new SelectedProduct(transportPackage.id(), 1),
            new SelectedProduct(internationalTransport.id(), 1),  // ← wybór W ŚRODKU Transport Package
            new SelectedProduct(insurancePackage.id(), 1),
            new SelectedProduct(extendedCoverage.id(), 1),        // ← wybór W ŚRODKU Insurance Package
            new SelectedProduct(notificationsPackage.id(), 1),
            new SelectedProduct(emailNotifications.id(), 1),      // ← wybór W ŚRODKU Notifications Package
            new SelectedProduct(smsNotifications.id(), 1)
        );

        PackageValidationResult result = shippingSolution.validateSelection(fullSelection);

        // ✅ PASS:
        // - International transport wymaga Extended/Premium insurance ✓
        // - Extended insurance został wybrany ✓
        assert result.isValid();
    }

    public void testScenario_International_Invalid_BasicInsurance() {
        PackageType shippingSolution = createCompleteShippingSolution();
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();

        // Klient wybiera:
        // - International Transport
        // - Basic Insurance (❌ błąd! International wymaga Extended/Premium!)

        List<SelectedProduct> fullSelection = List.of(
            new SelectedProduct(transportPackage.id(), 1),
            new SelectedProduct(internationalTransport.id(), 1),  // International!
            new SelectedProduct(insurancePackage.id(), 1),
            new SelectedProduct(basicCoverage.id(), 1)            // ❌ Basic not allowed!
        );

        PackageValidationResult result = shippingSolution.validateSelection(fullSelection);

        // ❌ FAIL: International wymaga Extended/Premium, nie Basic
        assert !result.isValid();
    }

    public void testScenario_Express_Invalid_NoNotifications() {
        PackageType shippingSolution = createCompleteShippingSolution();
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();

        // Klient wybiera:
        // - Express Transport
        // - Extended Insurance
        // - BRAK Notifications Package (❌ błąd! Express wymaga powiadomień!)

        List<SelectedProduct> fullSelection = List.of(
            new SelectedProduct(transportPackage.id(), 1),
            new SelectedProduct(expressTransport.id(), 1),        // Express!
            new SelectedProduct(insurancePackage.id(), 1),
            new SelectedProduct(extendedCoverage.id(), 1)
            // ❌ Brak notificationsPackage.id()!
        );

        PackageValidationResult result = shippingSolution.validateSelection(fullSelection);

        // ❌ FAIL: Express wymaga Notifications Package
        assert !result.isValid();
    }

    public void testScenario_Domestic_Valid() {
        PackageType shippingSolution = createCompleteShippingSolution();
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();

        // Klient wybiera:
        // - Domestic Transport (nie International, nie Express)
        // - Basic Insurance (OK! Reguły IF się nie aktywują)
        // - BRAK Notifications (OK! Opcjonalne i bez Express)

        List<SelectedProduct> fullSelection = List.of(
            new SelectedProduct(transportPackage.id(), 1),
            new SelectedProduct(domesticTransport.id(), 1),       // Domestic
            new SelectedProduct(insurancePackage.id(), 1),
            new SelectedProduct(basicCoverage.id(), 1)            // Basic OK
        );

        PackageValidationResult result = shippingSolution.validateSelection(fullSelection);

        // ✅ PASS: Domestic nie ma dodatkowych wymagań
        // Warunki IF nie są spełnione
        assert result.isValid();
    }

    // ===== KROK 7: Tworzenie instancji zagnieżdżonego pakietu =====

    public void createNestedPackageInstance() {
        PackageType shippingSolution = createCompleteShippingSolution();
        PackageType transportPackage = createTransportPackage();
        PackageType insurancePackage = createInsurancePackage();
        PackageType notificationsPackage = createNotificationsPackage();

        // Firma "LogiTech Sp. z o.o." zamawia Complete Shipping Solution

        // Ich wybory:
        List<SelectedProduct> selection = List.of(
            // Główny poziom - wybrane pakiety
            new SelectedProduct(transportPackage.id(), 1),
            new SelectedProduct(insurancePackage.id(), 1),
            new SelectedProduct(notificationsPackage.id(), 1),
            // Wewnątrz Transport Package
            new SelectedProduct(internationalTransport.id(), 1),
            // Wewnątrz Insurance Package
            new SelectedProduct(premiumCoverage.id(), 1),
            // Wewnątrz Notifications Package
            new SelectedProduct(emailNotifications.id(), 1),
            new SelectedProduct(smsNotifications.id(), 1),
            new SelectedProduct(pushNotifications.id(), 1)  // wszystkie 3 kanały
        );

        // Tworzymy instancję głównego pakietu
        PackageInstance mainPackageInstance = new InstanceBuilder(InstanceId.newOne())
            .withBatch(BatchId.of("SHIPPING-LOGITECH-2025-Q1"))
            .asPackageInstance(shippingSolution)
                .withSelection(selection)
                .build();

        // ✅ mainPackageInstance reprezentuje:
        // - Complete Shipping Solution (główny pakiet)
        // - Zawiera 3 sub-packages (Transport, Insurance, Notifications)
        // - Każdy sub-package ma konkretne wybory produktów
        // - Wszystko zwalidowane rekursywnie

        System.out.println("Created package instance: " + mainPackageInstance.id());
        System.out.println("Package type: " + mainPackageInstance.packageType().name());
        System.out.println("Total products selected: " + mainPackageInstance.selection().size());
        // Output:
        // Created package instance: <UUID>
        // Package type: Complete Shipping Solution
        // Total products selected: 8 (3 packages + 5 products inside them)
    }
}
```

### Wyjaśnienie zagnieżdżonych pakietów

**Composite Pattern w akcji:**

```
Complete Shipping Solution (PackageType - composite)
  │
  ├─ Transport Package (PackageType - composite)
  │    └─ International Transport (ProductType - leaf) ← wybrany
  │
  ├─ Insurance Package (PackageType - composite)
  │    └─ Premium Coverage (ProductType - leaf) ← wybrany
  │
  └─ Notifications Package (PackageType - composite)
       ├─ Email (ProductType - leaf) ← wybrany
       ├─ SMS (ProductType - leaf) ← wybrany
       └─ Push (ProductType - leaf) ← wybrany
```

**Kluczowe obserwacje:**

1. **PackageType może zawierać inne PackageType** - `transportPackage.id()` jest używane w `withSingleChoice()` głównego pakietu

2. **Selection musi zawierać WSZYSTKIE poziomy** - zarówno wybrane pakiety jak i produkty wewnątrz nich

3. **Reguły biznesowe sprawdzają produkty liściowe** - `IF international transport` patrzy na `internationalTransport.id()`, nie na `transportPackage.id()`

4. **Walidacja jest rekurencyjna** - główny pakiet waliduje zarówno wybór sub-packages jak i reguły oparte na produktach w środku

---

## Podsumowanie obu scenariuszy

### Banking Package (AND/OR/NOT)
- ✅ Złożone reguły logiczne: `AND(Extended, NOT(Basic), Mobile)`
- ✅ Conditional rules: `IF Premium THEN ...`
- ✅ ProductSets z różnych kategorii (accounts, cards, channels)
- ✅ Fluent builder API z `withSingleChoice()`, `withChoice()`

### Logistics Package (Nested)
- ✅ Pakiety w pakietach (Composite Pattern)
- ✅ 3 poziomy zagnieżdżenia: Solution → Packages → Products
- ✅ Reguły cross-package: `IF product in Package A THEN Package B required`
- ✅ Selection zawiera wszystkie poziomy hierarchii

Oba przykłady pokazują **moc abstrakcji** - te same mechanizmy (SelectionRule, ProductSet, PackageType) obsługują zarówno płaskie pakiety z logiką jak i głęboko zagnieżdżone struktury!
