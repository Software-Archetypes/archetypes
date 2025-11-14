# Nie dla psa kiełbasa - czyli warunki stosowalności produktów

## Wprowadzenie

Dzisiaj zajmiemy się problemem, który w prawdziwych systemach e-commerce jest absolutnie kluczowy, a w wielu aplikacjach kompletnie zaniedbany. Problem brzmi następująco: nie każdy produkt jest dla każdego klienta, w każdym miejscu i w każdym czasie. Brzmi banalnie? A jednak w większości systemów albo tego w ogóle nie ma, albo jest zakodowane na sztywno w dziesiątkach ifów rozrzuconych po całej aplikacji.

Tytuł tej lekcji - "Nie dla psa kiełbasa" - świetnie oddaje istotę problemu. To ludowe powiedzenie oznacza, że coś nie jest przeznaczone dla kogoś, nie należy mu się, nie jest dla niego odpowiednie. I dokładnie o to nam chodzi w warunkach stosowalności produktów.

## Problem biznesowy

Wyobraźcie sobie prawdziwy sklep internetowy. Macie w ofercie tysiące produktów, ale:

- Niektóre produkty można sprzedawać tylko w określonych krajach ze względu na regulacje prawne
- Pewne funkcje premium są dostępne tylko w aplikacji mobilnej
- Część usług jest dostępna tylko dla firm, inne tylko dla konsumentów indywidualnych
- Produkty medyczne mają ograniczenia wiekowe
- Sezonowe produkty są dostępne tylko w określonych miesiącach
- Niektóre produkty wymagają specjalnych uprawnień klienta

To nie są wymysły. To realne wymagania biznesowe. I pytanie brzmi: jak to modelować w kodzie, żeby nie skończyć z makaronem pełnym warunków rozrzuconych wszędzie?

## Kiedy sprawdzamy reguły stosowalności?

Zanim przejdziemy do kodu, musimy odpowiedzieć na fundamentalne pytanie: kiedy właściwie te reguły sprawdzamy?

Odpowiedź jest prozaiczna ale ważna: **w momencie filtrowania i prezentacji oferty**. Nie czekamy aż klient dodał coś do koszyka. Nie pokazujemy mu w ogóle produktów, które dla niego nie są dostępne. Albo pokazujemy, ale wyraźnie komunikujemy, że nie może ich kupić.

Może to być:
- Filtrowanie katalogu przed wyświetleniem
- Walidacja przy próbie dodania do koszyka
- Sprawdzenie uprawnień przy przeglądaniu szczegółów produktu
- Kontrola dostępu w procesie checkout

Gdzie te reguły mieszkają w kodzie? My przypinamy je do `ProductType`, ale równie dobrze mogłyby być w `CatalogEntry`. To jest decyzja projektowa zależna od kontekstu. Jeśli reguły są nieodłączną częścią definicji produktu - idą do `ProductType`. Jeśli zmieniają się w zależności od katalogu czy kampanii - mogą iść do `CatalogEntry`.

## Rozwiązanie: generyczny system reguł

Potrzebujemy czegoś, co:
1. Jest wystarczająco generyczne, żeby obsłużyć każdy przypadek biznesowy
2. Jest wystarczająco proste, żeby łatwo się z niego korzystało
3. Wspiera kompozycję - złożone warunki składają się z prostych
4. Jest łatwe do testowania

Nasze rozwiązanie składa się z trzech elementów:

### 1. ApplicabilityContext - kontekst ewaluacji

To jest generyczna mapa klucz-wartość reprezentująca sytuację, w której sprawdzamy stosowalność.

```java
public class ApplicabilityContext {
    private final Map<String, String> parameters;

    public static ApplicabilityContext of(Map<String, String> parameters) {
        return new ApplicabilityContext(parameters);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }
}
```

To jest po prostu opakowanie na mapę. Nic więcej. Moglibyśmy używać zwykłej `Map<String, String>`, ale dedykowana klasa daje nam type safety i możliwość ewolucji w przyszłości.

Przykład użycia:

```java
var context = ApplicabilityContext.of(Map.of(
    "country", "PL",
    "channel", "mobile",
    "age", "15",
    "customerType", "B2C"
));
```

### 2. ApplicabilityConstraint - reguły z pełną kompozycją

To jest serce systemu. Sealed interface z implementacjami reprezentującymi różne typy warunków:

```java
public sealed interface ApplicabilityConstraint permits
        EqualsConstraint,
        InConstraint,
        GreaterThanConstraint,
        LessThanConstraint,
        BetweenConstraint,
        AndConstraint,
        OrConstraint,
        NotConstraint,
        AlwaysTrueConstraint {

    boolean isSatisfiedBy(ApplicabilityContext context);
}
```

Mamy dwa rodzaje constraintów:

**Podstawowe warunki atomowe:**

```java
record EqualsConstraint(String parameterName, String expectedValue)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> value.equals(expectedValue))
                .orElse(false);
    }
}
```

Sprawdza czy wartość parametru równa się oczekiwanej wartości. Jeśli parametr nie istnieje w kontekście - zwraca `false`.

```java
record InConstraint(String parameterName, Set<String> allowedValues)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(allowedValues::contains)
                .orElse(false);
    }
}
```

Sprawdza czy wartość należy do zbioru dozwolonych wartości.

```java
record GreaterThanConstraint(String parameterName, int threshold)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> {
                    try {
                        return Integer.parseInt(value) > threshold;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }
}
```

Obsługa wartości numerycznych. Jeśli wartość nie da się sparsować do inta - constraint nie jest spełniony.

Podobnie `LessThanConstraint` i `BetweenConstraint`.

**Constrainty kompozytowe:**

To jest kluczowa część - możliwość budowania złożonych warunków z prostych.

```java
record AndConstraint(List<ApplicabilityConstraint> constraints)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return constraints.stream()
                .allMatch(c -> c.isSatisfiedBy(context));
    }
}
```

Koniunkcja - wszystkie warunki muszą być spełnione.

```java
record OrConstraint(List<ApplicabilityConstraint> constraints)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return constraints.stream()
                .anyMatch(c -> c.isSatisfiedBy(context));
    }
}
```

Alternatywa - wystarczy, że choć jeden warunek jest spełniony.

```java
record NotConstraint(ApplicabilityConstraint constraint)
    implements ApplicabilityConstraint {

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return !constraint.isSatisfiedBy(context);
    }
}
```

Negacja - odwraca wynik warunku.

**Fluent API:**

Żeby było wygodniej, mamy factory methods na interfejsie:

```java
static ApplicabilityConstraint equals(String parameterName, String expectedValue) {
    return new EqualsConstraint(parameterName, expectedValue);
}

static ApplicabilityConstraint in(String parameterName, String... allowedValues) {
    return new InConstraint(parameterName, Set.of(allowedValues));
}

static ApplicabilityConstraint and(ApplicabilityConstraint... constraints) {
    return new AndConstraint(Arrays.asList(constraints));
}

static ApplicabilityConstraint or(ApplicabilityConstraint... constraints) {
    return new OrConstraint(Arrays.asList(constraints));
}
```

Dzięki temu możemy pisać czytelny kod ze static importami.

### 3. Integracja z ProductType

Dodaliśmy do `ProductType` pole z regułą stosowalności:

```java
class ProductType {
    private final ApplicabilityConstraint applicabilityConstraint;

    ProductType(..., ApplicabilityConstraint applicabilityConstraint) {
        checkArgument(applicabilityConstraint != null,
            "ApplicabilityConstraint must be defined");
        this.applicabilityConstraint = applicabilityConstraint;
    }

    public boolean isApplicableFor(ApplicabilityContext context) {
        return applicabilityConstraint.isSatisfiedBy(context);
    }
}
```

Domyślnie każdy produkt ma `AlwaysTrueConstraint`:

```java
static ProductType identical(ProductIdentifier id,
                             ProductName name,
                             ProductDescription description,
                             Unit preferredUnit) {
    return new ProductType(id, name, description, preferredUnit,
        ProductTrackingStrategy.IDENTICAL,
        ProductFeatureTypes.empty(),
        ProductMetadata.empty(),
        ApplicabilityConstraint.alwaysTrue());  // <-- default
}
```

Builder pozwala na ustawienie własnej reguły:

```java
ProductType.builder(id, name, description, unit, trackingStrategy)
    .withApplicabilityConstraint(/* ... */)
    .build();
```

## Przykłady użycia

Teraz zobaczmy, jak to działa w praktyce. Zacznijmy od prostego przypadku:

**Produkt tylko na mobile:**

```java
var mobileOnlyProduct = ProductType.builder(
        id,
        ProductName.of("Mobile App Premium"),
        ProductDescription.of("Premium feature available only on mobile"),
        Unit.pieces(),
        ProductTrackingStrategy.IDENTICAL
    )
    .withApplicabilityConstraint(
        equals("channel", "mobile")
    )
    .build();

var mobileContext = ApplicabilityContext.of(Map.of("channel", "mobile"));
var webContext = ApplicabilityContext.of(Map.of("channel", "web"));

mobileOnlyProduct.isApplicableFor(mobileContext);  // true
mobileOnlyProduct.isApplicableFor(webContext);     // false
```

**Produkt dla wybranych krajów:**

```java
var euProduct = ProductType.builder(...)
    .withApplicabilityConstraint(
        in("country", "PL", "DE", "FR", "UK")
    )
    .build();
```

**Produkt z ograniczeniem wiekowym:**

```java
var adultProduct = ProductType.builder(...)
    .withApplicabilityConstraint(
        greaterThan("age", 18)
    )
    .build();
```

**Produkt dla nastolatków (zakres):**

```java
var teenProduct = ProductType.builder(...)
    .withApplicabilityConstraint(
        between("age", 13, 19)
    )
    .build();
```

Teraz złożone przypadki z kompozycją:

**Produkt pediatryczny - tylko PL/UK, tylko mobile/web, tylko poniżej 16 lat:**

```java
var pediatricService = ProductType.builder(...)
    .withApplicabilityConstraint(
        and(
            or(equals("country", "PL"), equals("country", "UK")),
            or(equals("channel", "mobile"), equals("channel", "web")),
            lessThan("age", 16)
        )
    )
    .build();

// Test 1: wszystko się zgadza
var validContext = ApplicabilityContext.of(Map.of(
    "country", "PL",
    "channel", "mobile",
    "age", "10"
));
pediatricService.isApplicableFor(validContext);  // true

// Test 2: za stary
var tooOld = ApplicabilityContext.of(Map.of(
    "country", "PL",
    "channel", "mobile",
    "age", "18"
));
pediatricService.isApplicableFor(tooOld);  // false

// Test 3: zły kraj
var wrongCountry = ApplicabilityContext.of(Map.of(
    "country", "DE",
    "channel", "mobile",
    "age", "10"
));
pediatricService.isApplicableFor(wrongCountry);  // false
```

Widzicie tutaj pełną moc kompozycji. Mamy trzy warunki połączone `AND`:
1. Kraj musi być PL **lub** UK
2. Kanał musi być mobile **lub** web
3. Wiek musi być **mniejszy** niż 16

To jest wyrażenie logiczne, które możemy czytać jak normalny język:

```
(country = PL OR country = UK)
AND
(channel = mobile OR channel = web)
AND
age < 16
```

## Moc kompozycji

Kluczowa własność tego rozwiązania to **pełna kompozycja**. Każdy constraint może zawierać inne constrainty, które mogą zawierać kolejne, i tak dalej bez ograniczeń.

Możemy budować dowolnie złożone wyrażenia:

```java
and(
    or(
        and(equals("country", "PL"), equals("customerType", "B2B")),
        and(equals("country", "UK"), equals("customerType", "B2C"))
    ),
    not(equals("channel", "desktop")),
    between("orderValue", 100, 10000)
)
```

To odpowiada wyrażeniu:

```
((country=PL AND customerType=B2B) OR (country=UK AND customerType=B2C))
AND
channel != desktop
AND
orderValue BETWEEN 100 AND 10000
```

Ten poziom ekspresywności pozwala nam obsłużyć praktycznie każdy przypadek biznesowy, jaki możemy sobie wyobrazić.

## Testy

Testy dla tego systemu są bardzo czytelne, bo doskonale pokazują intencję:

```java
@Test
void shouldAllowPediatricServiceForValidContext() {
    var pediatricService = ProductType.builder(...)
        .withApplicabilityConstraint(
            and(
                or(equals("country", "PL"), equals("country", "UK")),
                lessThan("age", 16)
            )
        )
        .build();

    var context = ApplicabilityContext.of(Map.of(
        "country", "PL",
        "age", "10"
    ));

    assertTrue(pediatricService.isApplicableFor(context));
}
```

Każdy test to mini-dokumentacja mówiąca: "ten produkt jest dostępny w takim kontekście, a nie jest dostępny w takim".

## Ciekawostka: związek z SAT/SMT

Dla zainteresowanych teorią: to co tutaj robimy jest **strukturalnie** związane z problemem SAT (Boolean Satisfiability).

Problem SAT brzmi: mając formułę logiczną, czy istnieje przypisanie wartości zmiennym, które sprawi, że formuła będzie prawdziwa?

SAT to jeden z fundamentalnych problemów w informatyce teoretycznej - pierwszy problem, dla którego udowodniono NP-zupełność. Solvery SAT/SMT są używane w weryfikacji formalnej, planowaniu, konfigurowaniu systemów.

**Ale uwaga** - my **nie rozwiązujemy** problemu SAT. My robimy coś znacznie prostszego:
- W SAT: mamy formułę i **szukamy** przypisania wartości
- U nas: mamy formułę i **dane** przypisanie wartości - sprawdzamy czy pasuje

To jest różnica między złożonością O(n) a NP-zupełnością. My po prostu ewaluujemy wyrażenie logiczne dla danego kontekstu. To jest banalne obliczeniowo.

Niemniej, sama **struktura** tego co budujemy - wyrażenia logiczne z AND, OR, NOT - jest taka sama jak w SAT. I to jest eleganckie, bo opieramy się na solidnych fundamentach teoretycznych, ale nie wchodzimy w nierozstrzygalność.

Dla ciekawskich polecam poczytać o:
- CNF (Conjunctive Normal Form) i DNF (Disjunctive Normal Form)
- Solwerach SAT/SMT (Z3, MiniSat)
- Zastosowaniach SAT w package dependency resolution (npm, apt)

To fascynujący temat, ale wykracza poza zakres tej lekcji.

## Gdzie to może być zapisane?

Skoro mówimy o regułach, które mogą być arbitralnie złożone, pojawia się pytanie: jak to zapisać w bazie danych?

Najprostsza odpowiedź: **JSON**. Wszystkie nowoczesne bazy relacyjne mają wsparcie dla JSON:
- PostgreSQL ma typ `JSONB` z indeksami i queryowaniem
- MySQL ma typ `JSON`
- SQLite od wersji 3.38 ma JSON functions

Przykładowy JSON dla naszego pediatric service:

```json
{
  "type": "AND",
  "constraints": [
    {
      "type": "OR",
      "constraints": [
        {"type": "EQUALS", "parameter": "country", "value": "PL"},
        {"type": "EQUALS", "parameter": "country", "value": "UK"}
      ]
    },
    {
      "type": "OR",
      "constraints": [
        {"type": "EQUALS", "parameter": "channel", "value": "mobile"},
        {"type": "EQUALS", "parameter": "channel", "value": "web"}
      ]
    },
    {
      "type": "LESS_THAN", "parameter": "age", "threshold": 16}
  ]
}
```

Potrzebujemy tylko prostego parsera JSON → Constraint i serializera Constraint → JSON. To jest standardowy visitor pattern albo pattern matching w Javie 21+.

## Podsumowanie

Zbudowaliśmy generyczny, elastyczny system reguł stosowalności produktów, który:

1. **Jest prosty w użyciu** - fluent API ze static imports
2. **Jest mocny** - pełna kompozycja pozwala wyrazić dowolny warunek
3. **Jest testowalny** - każdy przypadek to osobny, czytelny test
4. **Jest generyczny** - nie narzuca żadnej domeny biznesowej
5. **Jest łatwy do persystencji** - można zapisać jako JSON
6. **Opiera się na solidnych fundamentach** - algebra Boole'a

Najważniejsze: **oddzieliliśmy reguły biznesowe od logiki aplikacji**. Zamiast ifów rozrzuconych po kontrolerach, serwisach i widokach, mamy deklaratywne reguły przypięte do modelu domeny.

To pozwala:
- Łatwo zmieniać reguły bez zmian w kodzie aplikacji
- Testować reguły niezależnie od reszty systemu
- Potencjalnie przenieść definicję reguł do konfiguracji (JSON)
- Centralnie zarządzać logiką "kto może co"

I pamiętajcie - to nie jest overengineering. To jest inwestycja w przyszłość. Pierwsze wymaganie "tylko mobile" możecie zakodować na sztywno. Drugie "tylko dla PL" możecie dodać kolejnego ifa. Ale przy dziesiątym warunku macie już makaron. A z tym systemem - dodajecie kolejny constraint i lecicie dalej.

Nie dla psa kiełbasa - ale dla właściwego psa, we właściwym miejscu, we właściwym czasie.
