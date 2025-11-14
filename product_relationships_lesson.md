# Co gdy produkty wiedzą o sobie nawzajem?

## Problem

W prawdziwym świecie produkty rzadko żyją w izolacji. Kupując kawę, dostajesz propozycję większego rozmiaru. Zamawiając burgera, słyszysz „Czy frytki do tego?". Wybierając nowy plan taryfowy, stary musi zostać wyłączony. Czasem szukasz produktu kompatybilnego z tym, co już masz – jak adapter do fotelika, który musi pasować zarówno do wózka, jak i do samochodu.

To właśnie relacje między produktami. Sposób, w jaki produkty łączą się ze sobą, zastępują, wykluczają, albo tworzą większą całość.

Problem w tym, że w większości systemów te relacje… wcale nie istnieją. Przynajmniej nie w modelu danych.

## Jak to wygląda zwykle?

Zależności między produktami żyją w dokumentacji, w arkuszach Excela, w umowach z partnerami biznesowymi, albo w głowach ludzi. A jeśli już trafiają do kodu, to w dwóch formach.

**Forma pierwsza: jawna ifologia**

```java
if (product.equals("CreditCard") && !hasAccount(customer)) {
    throw new BusinessException("Karta wymaga konta.");
}

if (oldPlan.equals("Premium") && newPlan.equals("Basic")) {
    throw new BusinessException("Nie można zmienić Premium na Basic.");
}
```

Każda reguła zakodowana na sztywno. Każda nowa kombinacja produktów wymaga zmiany w kodzie. Biznes chce połączyć dwa produkty w pakiet? „To potrwa dwa sprinty".

**Forma druga: ukryte zależności w danych**

Zamiast zapisać gdziekolwiek regułę, że karta kredytowa zależy od konta, po prostu dopisujemy kolumnę:

```java
class BankAccount {
    String accountNumber;
    String customerId;
    BigDecimal balance;
    String creditCardNumber; // bo karta "musi być gdzieś"
}
```

A później pojawia się `loanId`, `insurancePolicyId`, `blikAlias`. Klasa reprezentująca rachunek zmienia się w centrum wszechświata. Setki miejsc w kodzie sprawdza, czy pole jest nullem, bo od tego zależy logika procesów.

## Co z tego wynika?

Po pierwsze – **ograniczona elastyczność**. Każda nowa kombinacja produktów wymaga zmian w kodzie. System nie nadąża za biznesem.

Po drugie – **błędogenność**. Reguły istnieją tylko w kodzie i ludzkiej pamięci. Karta bez konta, paczka bez adresu – model nie pilnuje spójności.

Po trzecie – **brak skalowalności funkcjonalnej**. Nowe funkcje powstają przez dokładanie pól, flag i wyjątków. System przestaje rosnąć – zaczyna się kruszyć.

## Nasza odpowiedź

Relacje między produktami nie są dodatkiem. To szkielet oferty, który decyduje, co można połączyć, co się wyklucza, i jak produkty współistnieją.

Modelujemy to jawnie. Jako obywateli pierwszej kategorii w domenie.

## Typy relacji

Zdefiniowaliśmy sześć typów relacji między produktami. Wszystkie są **asymetryczne** – mają kierunek. Relacja od A do B to nie to samo, co od B do A.

```java
public enum ProductRelationshipType {
    UPGRADABLE_TO,
    SUBSTITUTED_BY,
    REPLACED_BY,
    COMPLEMENTED_BY,
    COMPATIBLE_WITH,
    INCOMPATIBLE_WITH
}
```

**UPGRADABLE_TO** – produkt może być zaktualizowany do produktu o wyższej specyfikacji. To fundament up-sellingu. Kawa mała może być zaktualizowana do dużej za dodatkowe 25 centów.

**SUBSTITUTED_BY** – produkt może być zastąpiony innym, gdy oryginalny jest niedostępny. Model laptopa X może być zastąpiony modelem Y jako alternatywa.

**REPLACED_BY** – produkt jest przestarzały i **musi** zostać zastąpiony. Stara wersja oprogramowania została zastąpiona nową – wymuszony upgrade.

**COMPLEMENTED_BY** – produkt może być uzupełniony przez komplementarny produkt. Cross-selling. „Burger → Frytki" – „Czy chcesz frytki do tego?".

**COMPATIBLE_WITH** – produkty są kompatybilne. Adapter do fotelika jest kompatybilny z wózkiem model X.

**INCOMPATIBLE_WITH** – produkty się wykluczają. Plan taryfowy Premium wyklucza się z planem Basic. Nie można mieć obu jednocześnie.

## Model danych

Relacja to prosta struktura:

```java
public record ProductRelationship(
        ProductRelationshipId id,
        ProductIdentifier from,
        ProductIdentifier to,
        ProductRelationshipType type) {
}
```

Identyfikator relacji, dwa produkty (`from` i `to`), oraz typ relacji. To wszystko.

Zauważcie: **nie ma tu ról**. W module Party mieliśmy `PartyRole` – ta sama Party mogła występować w różnych rolach w różnych kontekstach. Jan jako Employee, Jan jako Customer.

Ale w produktach? ProductType ma swoją tożsamość i to wystarcza. Nie potrzebujemy „Kawa Mała w roli X". Kawa Mała to Kawa Mała. Typ relacji już wszystko wyjaśnia.

To uproszczenie. Świadome. Produkty nie są tak kontekstowe jak ludzie czy organizacje.

## Struktura kodu

Zbudowaliśmy pełną infrastrukturę do zarządzania relacjami:

**ProductRelationshipId** – identyfikator oparty na UUID. Każda relacja ma unikalny ID.

**ProductRelationshipType** – enum z sześcioma typami relacji.

**ProductRelationship** – record reprezentujący relację.

**ProductRelationshipRepository** – interfejs do persystencji relacji. Ma metodę `findAllRelationsFrom` – znajdź wszystkie relacje wychodzące z danego produktu. Możesz też filtrować po typie – znajdź wszystkie `COMPLEMENTED_BY` dla burgera.

**InMemoryProductRelationshipRepository** – implementacja w pamięci, w tym samym pliku co interfejs. Używa `ConcurrentHashMap` dla bezpieczeństwa wielowątkowego.

**ProductRelationshipsQueries** – warstwa query. Pytasz: „Jakie produkty uzupełniają burger?". Dostajesz listę.

**ProductRelationshipFactory** – tworzy relacje. Sprawdza policy – czy taka relacja może w ogóle powstać.

**ProductRelationshipDefiningPolicy** – interfejs polityki. Decyduje, czy relacja może zostać zdefiniowana. Domyślnie mamy `AlwaysAllowProductRelationshipDefiningPolicy` – zawsze pozwala. Ale możesz zaimplementować własne.

Przykłady policy:

- **NoSelfRelationshipPolicy** – produkt nie może mieć relacji sam ze sobą.
- **NoCyclicUpgradePolicy** – jeśli A jest `UPGRADABLE_TO` B, to B nie może być `UPGRADABLE_TO` A.
- **MaxComplementsPolicy** – burger może mieć maksymalnie 5 dodatków.
- **CompositePolicy** – łączy wiele polityk. Wszystkie muszą się zgodzić.

**ProductRelationshipsFacade** – punkt wejścia. Publiczne API modułu.

Fasada ma dwie operacje:

```java
public Result<String, ProductRelationship> defineRelationship(
        ProductIdentifier from,
        ProductIdentifier to,
        ProductRelationshipType type)

public Result<String, ProductRelationshipId> removeRelationship(
        ProductRelationshipId relationshipId)
```

Definiujesz relację – system sprawdza, czy oba produkty istnieją, czy policy się zgadza, i zapisuje. Zwraca `Result` – albo sukces z relacją, albo porażka ze stringiem opisującym błąd.

Usuwasz relację – podajesz ID, system usuwa. Proste.

## Przykłady użycia

**Up-selling w kawiarni:**

```java
facade.defineRelationship(
    smallCoffee.identifier(),
    largeCoffee.identifier(),
    UPGRADABLE_TO
);
```

System wie: kawa mała może być zaktualizowana do dużej. Kasjer widzi propozycję. Klient płaci różnicę. Up-selling działa.

**Cross-selling w fast foodzie:**

```java
facade.defineRelationship(burger.identifier(), fries.identifier(), COMPLEMENTED_BY);
facade.defineRelationship(burger.identifier(), coke.identifier(), COMPLEMENTED_BY);
```

Zamawiasz burgera, system podpowiada frytki i colę. Cross-selling.

**Wykluczanie planów taryfowych:**

```java
facade.defineRelationship(premiumPlan.identifier(), basicPlan.identifier(), INCOMPATIBLE_WITH);
```

Masz Premium, nie możesz mieć jednocześnie Basic. System pilnuje.

**Kompatybilność akcesoriów:**

```java
facade.defineRelationship(adapter.identifier(), strollerModelX.identifier(), COMPATIBLE_WITH);
facade.defineRelationship(adapter.identifier(), carSeatModelY.identifier(), COMPATIBLE_WITH);
```

Adapter pasuje do wózka X i fotelika Y. Klient szuka kompatybilnych produktów – system wie.

## Queries w akcji

Chcesz wiedzieć, co uzupełnia burger?

```java
var complements = queries.findAllRelationsFrom(
    burger.identifier(),
    COMPLEMENTED_BY
);
```

Dostajesz listę relacji. Każda ma `from`, `to`, `type`. Możesz wyciągnąć `to` i pokazać klientowi propozycje.

Chcesz sprawdzić, czy dwa produkty się wykluczają?

```java
var incompatibilities = queries.findMatching(
    rel -> rel.from().equals(productA.identifier())
        && rel.to().equals(productB.identifier())
        && rel.type() == INCOMPATIBLE_WITH
);
```

Jeśli lista nie jest pusta – wykluczają się.

## Testy

Napisaliśmy testy dla fasady. Sprawdzają:

- **Błąd, gdy produkt źródłowy nie istnieje** – system zwraca `PRODUCT_NOT_FOUND`.
- **Błąd, gdy produkt docelowy nie istnieje** – analogicznie.
- **Sukces przy poprawnych produktach** – relacja zostaje zapisana, można ją odczytać.
- **Usuwanie relacji** – relacja znika z repozytorium.
- **Znajdowanie wszystkich relacji danego typu** – burger ma dwie `COMPLEMENTED_BY`, system zwraca obie.

Testy pokazują, że model działa. Fasada pilnuje niezmienników (produkty muszą istnieć), repository trzyma dane, queries zwracają to, co trzeba.

## Dlaczego to ma sens?

**Elastyczność biznesowa.** Biznes chce nowy pakiet? Nie zmieniasz kodu. Definiujesz relacje. Burger + Frytki + Cola jako `COMPLEMENTED_BY`. System wie, jak to sprzedać.

**Jawność reguł.** Relacje nie są ukryte w ifach ani w nullowych polach. Są danymi. Możesz je wyświetlić, zarządzać nimi, analizować.

**Skalowalność.** Nowe produkty, nowe kombinacje – dodajesz relacje, nie kod. System rośnie bez pękania.

**Policy jako punkty rozszerzenia.** Chcesz ograniczyć liczbę dodatków? Piszesz policy. Chcesz zabronić cyklicznych upgrade'ów? Policy. Logika biznesowa ma swoje miejsce.

## Relacje między ProductType czy CatalogEntry?

Na razie modelujemy relacje między `ProductType`. Kawa Mała jako typ produktu może być zaktualizowana do Kawy Dużej jako typu.

Ale nic nie stoi na przeszkodzie, żeby relacje istniały między `CatalogEntry`. Może ten sam ProductType w różnych katalogach ma różne relacje? Katalog dla klientów korporacyjnych, katalog dla indywidualnych – różne kombinacje, różne zasady.

To decyzja biznesowa. Model daje możliwość obu podejść. Relacja to `ProductIdentifier from`, `ProductIdentifier to`. ProductIdentifier może wskazywać na ProductType, CatalogEntry, a nawet ProductInstance, jeśli biznes tego potrzebuje.

## Podsumowanie

Relacje między produktami to nie dodatek. To szkielet oferty.

Bez nich system nie rozumie biznesu. Działa, ale nie wie dlaczego. Nie wie, co można połączyć, co się wyklucza, co się uzupełnia.

Modelując relacje jawnie, system zaczyna rozumieć kontekst. Nie tylko „co sprzedajesz", ale też „z czym i po co".

To moment, w którym model przestaje być katalogiem. Staje się narzędziem biznesowym.

Produkty wiedzą o sobie nawzajem. System wie, jak to wykorzystać.
