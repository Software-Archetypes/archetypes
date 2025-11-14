# Poziom 3 — Cechy i konfiguracje produktów
## (czyli: to, co sprawia, że produkt nie jest już tylko etykietą)

## Problem

Do tej pory mówiliśmy o tym, czym produkt jest — czy to unikat, czy egzemplarz z katalogu, czy może identyczny towar liczony w kilogramach.

Ale teraz wchodzimy głębiej. Bo nawet jeśli mamy produkt, to zazwyczaj nie chcemy go sprzedawać w jednej, sztywnej formie.

Chcemy, żeby klient mógł wybierać. Żeby mógł powiedzieć:
- „chcę ten sam model, ale w czerwonym kolorze",
- „chcę wizytę u lekarza specjalisty, ale tylko u profesora",
- albo „chcę transport, ale przesyłkę ponadgabarytową".

I właśnie w tym momencie pojawia się konfiguracja.

### Cechy – czyli jak odróżnić „to samo" od „czegoś innego"

Każdy produkt — fizyczny, cyfrowy czy usługowy — ma zestaw cech, które definiują jego charakterystykę. To właśnie one sprawiają, że „ta sama rzecz" może mieć dziesiątki wariantów.

**Sklep**
Kupując koszulkę w sklepie, wybierasz nie tylko ile chcesz, ale też jaki – kolor, rozmiar, materiał, kształt.

```java
class TShirt {
   String name = "Koszulka Classic";
   String color = "Czerwony";
   String size = "M";
   String fabric = "Bawełna";
}
```

Wszystkie te koszulki to „ten sam produkt" z katalogu, ale każda konfiguracja to inna pozycja w magazynie.

**Przychodnia**
W przychodni szukasz lekarza — ale nie byle jakiego. Potrzebujesz specjalisty, może profesora, a do tego kogoś, kto przyjmuje dzieci.

```java
class MedicalAppointment {
   String name = "Konsultacja lekarska";
   String specialization = "Kardiologia dziecięca";
   String grade = "Profesor";
}
```

**Bankowość**
Lokata w banku to nie zawsze to samo. Może być krótkoterminowa, długoterminowa, dla kwot powyżej 100 000 PLN albo dla nowych klientów.

```java
class DepositOffer {
   String name = "Lokata Oszczędnościowa";
   int termMonths = 12;
   BigDecimal minAmount = new BigDecimal("100000");
   String channel = "mobile";
}
```

Jedna definicja, wiele wariantów, każdy z własnym zestawem reguł i ograniczeń.

**Logistyka**
Usługa „transport paczki" także może być inna dla każdej paczki

```java
class ShipmentOption {
   String name = "Paczka krajowa 24h";
   String size = "XL";
   boolean fragile = true;
   boolean oversize = true;
}
```

To dalej ta sama oferta, ale parametry — rozmiar, czy delikatna zawartość, czy ponadwymiarowa — zmieniają zasady gry: koszt, czas, sposób obsługi, kanał realizacji.

### Konsekwencje typowego modelowania

W rzeczywistych systemach tych pól są nie 2 czy 3, a dziesiątki. Konsekwencje najczęściej nie są pozytywne:

- **Niska elastyczność** – każda nowa cecha to zmiana schematu bazy lub kodu
- **Brak spójności semantycznej** – „rozmiar" w jednym systemie to enum, w drugim string, w trzecim słownik
- **Brak możliwości porównywania produktów** – wyszukiwarka nie wie, które cechy są wspólne, a które unikalne
- **Brak możliwości konfigurowania dynamicznego** – wszystko trzeba kodować na sztywno
- **Wysoki koszt utrzymania** – każdy nowy wariant to de facto nowy produkt w kodzie

Jeśli system nie potrafi odróżnić definicji produktu od jego konfiguracji, to każda nowa cecha staje się zmianą programistyczną, a każdy nowy wariant — mini-projektem IT.

---

## Rozwiązanie: Archetypy Product Feature Type i Product Feature Instance

Prawdziwie dojrzały model produktu musi pozwalać dodawać cechy i ich wartości **bez zmiany kodu**. Dopiero wtedy organizacja przestaje rozwijać ofertę w Excelu, a zaczyna zarządzać nią świadomie — w modelu.

### Architektura rozwiązania

Nasze rozwiązanie składa się z trzech warstw:

1. **Typy wartości** (`FeatureValueType`) — bezpieczny zestaw typów danych
2. **Ograniczenia** (`FeatureValueConstraint`) — reguły walidacji wartości
3. **Definicje i instancje** (`ProductFeatureType`, `ProductFeatureInstance`) — co można skonfigurować i jakie są konkretne wartości

#### Warstwa 1: Typy wartości

Zamiast pozwalać na dowolne typy (co prowadziłoby do chaosu), definiujemy bezpieczny zestaw:

```java
enum FeatureValueType {
    TEXT,        // teksty: "czerwony", "M", "profesor"
    INTEGER,     // liczby całkowite: rok produkcji, ilość miesięcy
    DECIMAL,     // liczby dziesiętne: kwoty, wymiary
    DATE,        // daty: daty ważności, terminy
    BOOLEAN      // flagi: czy fragile, czy premium
}
```

Każdy typ wie, jak przekonwertować się ze String (dla persystencji) i na String (dla wyświetlania):

```java
// Przykład: typ INTEGER
INTEGER.castFrom("2023")  // zwraca Integer(2023)
INTEGER.castTo(2023)      // zwraca "2023"
```

**Co się tu dzieje?** Definiujemy uniwersalny słownik typów, który działa w każdej branży. Nie ma „specjalnych typów dla e-commerce" czy „osobnych dla bankowości". Jest jeden zestaw, który obsługuje wszystkie przypadki.

#### Warstwa 2: Ograniczenia (Constraints)

Typ to za mało. Musimy powiedzieć, **jakie wartości są dozwolone**. Tu wchodzą ograniczenia:

**AllowedValuesConstraint** — lista dozwolonych wartości (dla tekstów)

```java
// Rozmiary koszulek: tylko S, M, L, XL
AllowedValuesConstraint.of("S", "M", "L", "XL")

// Specjalizacje lekarskie
AllowedValuesConstraint.of("Kardiologia", "Dermatologia", "Ortopedia")

// Kanały dystrybucji
AllowedValuesConstraint.of("mobile", "web", "branch")
```

**NumericRangeConstraint** — zakres liczb całkowitych

```java
// Rok produkcji: 2020-2024
new NumericRangeConstraint(2020, 2024)

// Długość lokaty w miesiącach: 1-36
new NumericRangeConstraint(1, 36)

// Liczba dni przechowywania paczki: 1-14
new NumericRangeConstraint(1, 14)
```

**DecimalRangeConstraint** — zakres liczb dziesiętnych

```java
// Minimalna kwota lokaty: 1000-1000000
DecimalRangeConstraint.of("1000.00", "1000000.00")

// Waga paczki: 0.1-30.0 kg
DecimalRangeConstraint.of("0.1", "30.0")
```

**DateRangeConstraint** — zakres dat

```java
// Data ważności produktu: 2024-01-01 do 2024-12-31
DateRangeConstraint.of("2024-01-01", "2024-12-31")

// Termin wizyty: najbliższe 90 dni
DateRangeConstraint.of(
    LocalDate.now().toString(),
    LocalDate.now().plusDays(90).toString()
)
```

**RegexConstraint** — walidacja wzorcem

```java
// Kod produktu: dwie litery + cztery cyfry (np. AB-1234)
new RegexConstraint("^[A-Z]{2}-\\d{4}$")

// Numer IBAN
new RegexConstraint("^PL\\d{26}$")
```

**Co się tu dzieje?** Każde ograniczenie wie, jak sprawdzić, czy wartość jest poprawna. Nie ma hard-kodowanych if-ów w kodzie biznesowym. Zasady są w modelu, nie w logice aplikacji.

#### Warstwa 3: Definicje cech (ProductFeatureType)

Teraz łączymy typ z ograniczeniem w **definicję cechy**:

```java
// Cecha: rozmiar (tekst z dozwolonymi wartościami)
ProductFeatureType sizeFeature =
    ProductFeatureType.withAllowedValues("size", "S", "M", "L", "XL");

// Cecha: rok produkcji (liczba w zakresie)
ProductFeatureType yearFeature =
    ProductFeatureType.withNumericRange("yearOfProduction", 2020, 2024);

// Cecha: minimalna kwota (decimal w zakresie)
ProductFeatureType minAmountFeature =
    ProductFeatureType.withDecimalRange("minAmount", "1000.00", "1000000.00");

// Cecha: data ważności (data w zakresie)
ProductFeatureType expiryFeature =
    ProductFeatureType.withDateRange("expiryDate", "2024-01-01", "2024-12-31");
```

**Co się tu dzieje?** Definiujemy, jakie cechy może mieć produkt. To jest **metadane** — opis tego, co można skonfigurować. Nie tworzymy jeszcze konkretnych produktów, tylko mówimy: „produkty tego typu będą miały takie i takie parametry do wyboru".

---

## Przykłady dla różnych branż

### Retail: Koszulki w e-commerce

**Krok 1: Definicja ProductType z cechami**

Tworzymy typ produktu „Koszulka Classic" i mówimy: ten produkt ma trzy cechy — kolor, rozmiar i materiał.

```java
// Definiujemy cechy, które klient może wybrać
ProductFeatureType colorFeature =
    ProductFeatureType.withAllowedValues("color", "czerwony", "niebieski", "czarny", "biały");

ProductFeatureType sizeFeature =
    ProductFeatureType.withAllowedValues("size", "S", "M", "L", "XL", "XXL");

ProductFeatureType fabricFeature =
    ProductFeatureType.withAllowedValues("fabric", "bawełna", "poliester", "mieszanka");

// Tworzymy ProductType: "Koszulka Classic"
ProductType tshirtType = ProductType.builder(
        new UuidProductIdentifier(),
        ProductName.of("Koszulka Classic"),
        ProductDescription.of("Uniwersalna koszulka casual"),
        Unit.pieces(),
        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
    )
    .withMandatoryFeature(colorFeature)    // Kolor jest obowiązkowy
    .withMandatoryFeature(sizeFeature)     // Rozmiar jest obowiązkowy
    .withOptionalFeature(fabricFeature)    // Materiał opcjonalny (domyślnie bawełna)
    .build();
```

**Co się tu dzieje?**
Mówimy systemowi: „Mam produkt Koszulka Classic. Żeby ją sprzedać, MUSZĘ wiedzieć jaki kolor i rozmiar. Materiał mogę, ale nie muszę podawać".

Teraz w systemie mamy **jedną definicję produktu**, ale klient może go kupić w **30 wariantach** (4 kolory × 5 rozmiarów × 3 materiały = 60, ale materiał opcjonalny, więc minimum 20).

**Krok 2: Konkretna instancja — koszulka w magazynie**

Klient zamawia: „Chcę czerwoną koszulkę w rozmiarze M z bawełny".

```java
// Tworzymy konkretną koszulkę w magazynie
ProductInstance redTshirtM = ProductInstance.builder()
    .id(ProductInstanceId.random())
    .type(tshirtType)
    .serial(TextualSerialNumber.of("TSH-2024-001234"))
    .withFeature(colorFeature, "czerwony")     // Ta konkretna sztuka jest czerwona
    .withFeature(sizeFeature, "M")             // Ta konkretna sztuka ma rozmiar M
    .withFeature(fabricFeature, "bawełna")     // Ta konkretna sztuka jest z bawełny
    .build();
```

**Co się tu dzieje?**
Tworzymy konkretną sztukę koszulki. System sprawdza:
1. Czy podaliśmy wszystkie mandatory features (color, size)? ✓
2. Czy wartości są zgodne z definicją (czy "czerwony" jest na liście dozwolonych kolorów)? ✓
3. Czy optional features, jeśli podane, są poprawne? ✓

Jeśli coś jest nie tak, rzuci wyjątek już przy tworzeniu obiektu.

**Krok 3: Dodanie nowej cechy bez zmiany kodu**

Marketing mówi: „Chcemy dodać opcję 'nadruk' — może być logo lub tekst".

Nie zmieniamy kodu aplikacji. Dodajemy nową cechę do definicji:

```java
ProductFeatureType printFeature =
    ProductFeatureType.withAllowedValues("print", "logo", "tekst", "brak");

// Dodajemy do definicji (w bazie lub w konfiguracji):
ProductType updatedTshirtType = ProductType.builder(
        tshirtType.id(),
        tshirtType.name(),
        tshirtType.description(),
        tshirtType.preferredUnit(),
        tshirtType.trackingStrategy()
    )
    .withMandatoryFeature(colorFeature)
    .withMandatoryFeature(sizeFeature)
    .withOptionalFeature(fabricFeature)
    .withOptionalFeature(printFeature)      // Nowa cecha — bez zmiany kodu!
    .build();
```

Od teraz każda nowa koszulka może mieć nadruk, a stare (bez tej cechy) dalej działają.

---

### Bankowość: Lokat bankowych

**Krok 1: Definicja ProductType dla lokaty**

Bank oferuje „Lokatę Oszczędnościową". Ma różne warianty: różne okresy, różne minimalne kwoty, różne kanały dystrybucji.

```java
// Cechy lokaty
ProductFeatureType termFeature =
    ProductFeatureType.withNumericRange("termMonths", 1, 36);

ProductFeatureType minAmountFeature =
    ProductFeatureType.withDecimalRange("minAmount", "1000.00", "1000000.00");

ProductFeatureType channelFeature =
    ProductFeatureType.withAllowedValues("channel", "mobile", "web", "branch");

ProductFeatureType customerTypeFeature =
    ProductFeatureType.withAllowedValues("customerType", "new", "existing", "premium");

// Definicja produktu: Lokata Oszczędnościowa
ProductType depositType = ProductType.builder(
        new UuidProductIdentifier(),
        ProductName.of("Lokata Oszczędnościowa"),
        ProductDescription.of("Lokata z elastycznym okresem i kwotą"),
        Unit.of("contract"),
        ProductTrackingStrategy.IDENTICAL
    )
    .withMandatoryFeature(termFeature)           // Okres lokaty obowiązkowy
    .withMandatoryFeature(minAmountFeature)      // Min. kwota obowiązkowa
    .withOptionalFeature(channelFeature)         // Kanał opcjonalny
    .withOptionalFeature(customerTypeFeature)    // Typ klienta opcjonalny
    .build();
```

**Co się tu dzieje?**
Bank definiuje: „Lokata to produkt, który ma zawsze określony okres i minimalną kwotę. Może dodatkowo mieć przypisany kanał dystrybucji i typ klienta".

To nie są atrybuty bazy danych. To są **reguły biznesowe zapisane w modelu**.

**Krok 2: Konkretna oferta dla klienta**

Klient przychodzi do banku: „Chcę lokatę na 12 miesięcy, minimum 50 000 PLN, przez aplikację mobilną".

```java
ProductInstance depositOffer = ProductInstance.builder()
    .id(ProductInstanceId.random())
    .type(depositType)
    .serial(TextualSerialNumber.of("DEP-2024-000456"))
    .withFeature(termFeature, 12)                      // 12 miesięcy
    .withFeature(minAmountFeature, new BigDecimal("50000.00"))  // 50k PLN
    .withFeature(channelFeature, "mobile")             // Przez aplikację
    .withFeature(customerTypeFeature, "existing")      // Istniejący klient
    .build();
```

**Co się tu dzieje?**
System tworzy konkretną instancję oferty. Sprawdza:
- Czy 12 miesięcy mieści się w zakresie 1-36? ✓
- Czy 50 000 PLN mieści się w zakresie 1000-1000000? ✓
- Czy "mobile" jest na liście kanałów? ✓

Jeśli bank jutro zmieni zasady (np. maksymalnie 24 miesiące), wystarczy zmienić constraint. Nie trzeba zmieniać kodu aplikacji.

**Krok 3: Różnicowanie ofert dla różnych segmentów**

Bank chce wprowadzić „Lokatę Premium" — tylko dla klientów premium, wyższe kwoty, dłuższe okresy.

Nie tworzymy nowego ProductType. Zmieniamy constraints:

```java
ProductFeatureType premiumTermFeature =
    ProductFeatureType.withNumericRange("termMonths", 12, 60);  // Dłuższe okresy

ProductFeatureType premiumMinAmountFeature =
    ProductFeatureType.withDecimalRange("minAmount", "100000.00", "5000000.00");  // Wyższe kwoty

ProductType premiumDepositType = ProductType.builder(
        new UuidProductIdentifier(),
        ProductName.of("Lokata Premium"),
        ProductDescription.of("Lokata dla klientów Premium"),
        Unit.of("contract"),
        ProductTrackingStrategy.IDENTICAL
    )
    .withMandatoryFeature(premiumTermFeature)
    .withMandatoryFeature(premiumMinAmountFeature)
    .withMandatoryFeature(customerTypeFeature)  // Teraz obowiązkowe: tylko "premium"
    .build();
```

**Co się tu dzieje?**
Mamy dwa produkty w katalogu: zwykła lokata i premium. Różnią się constraints. System automatycznie zapewnia, że nikt nie utworzy „Lokaty Premium" dla kwoty 1000 PLN — walidacja jest w modelu.

---

### Telekomunikacja: Usługa transportowa paczek

**Krok 1: Definicja ProductType dla przesyłki**

Firma kurierska oferuje „Paczkę Krajową 24h". Różne paczki mają różne cechy: rozmiar, czy jest delikatna, czy ponadgabarytowa, data odbioru.

```java
// Cechy przesyłki
ProductFeatureType sizeFeature =
    ProductFeatureType.withAllowedValues("size", "S", "M", "L", "XL");

ProductFeatureType fragileFeature =
    ProductFeatureType.unconstrained("fragile", FeatureValueType.BOOLEAN);

ProductFeatureType oversizeFeature =
    ProductFeatureType.unconstrained("oversize", FeatureValueType.BOOLEAN);

ProductFeatureType pickupDateFeature =
    ProductFeatureType.withDateRange("pickupDate",
        LocalDate.now().toString(),
        LocalDate.now().plusDays(14).toString()
    );

ProductFeatureType weightFeature =
    ProductFeatureType.withDecimalRange("weight", "0.1", "30.0");

// Definicja usługi: Paczka Krajowa 24h
ProductType parcelType = ProductType.builder(
        new UuidProductIdentifier(),
        ProductName.of("Paczka Krajowa 24h"),
        ProductDescription.of("Przesyłka krajowa z dostawą następnego dnia roboczego"),
        Unit.of("shipment"),
        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
    )
    .withMandatoryFeature(sizeFeature)        // Rozmiar obowiązkowy
    .withMandatoryFeature(weightFeature)      // Waga obowiązkowa
    .withOptionalFeature(fragileFeature)      // Flaga "delikatne" opcjonalna
    .withOptionalFeature(oversizeFeature)     // Flaga "ponadgabarytowa" opcjonalna
    .withOptionalFeature(pickupDateFeature)   // Data odbioru opcjonalna
    .build();
```

**Co się tu dzieje?**
Firma kurierska mówi: „Każda paczka musi mieć określony rozmiar i wagę. Opcjonalnie może być oznaczona jako delikatna, ponadgabarytowa, i może mieć wybraną datę odbioru".

**Krok 2: Konkretna przesyłka**

Klient wysyła paczkę: rozmiar XL, 15 kg, delikatna, odbiór jutro.

```java
ProductInstance parcel = ProductInstance.builder()
    .id(ProductInstanceId.random())
    .type(parcelType)
    .serial(TextualSerialNumber.of("PKG-2024-789012"))
    .withFeature(sizeFeature, "XL")
    .withFeature(weightFeature, new BigDecimal("15.0"))
    .withFeature(fragileFeature, true)
    .withFeature(oversizeFeature, false)
    .withFeature(pickupDateFeature, LocalDate.now().plusDays(1))
    .build();
```

**Co się tu dzieje?**
System tworzy konkretną przesyłkę. Sprawdza:
- Czy XL jest dozwolonym rozmiarem? ✓
- Czy 15 kg mieści się w zakresie 0.1-30.0? ✓
- Czy data odbioru jest w dozwolonym zakresie (dziś + 14 dni)? ✓

Teraz system wie, że ta paczka wymaga specjalnej obsługi (fragile=true) i może naliczyć odpowiednią cenę za ponadgabaryt.

**Krok 3: Dostęp do cech w logice biznesowej**

Później, w procesie wyceny lub routingu, system może odczytać cechy:

```java
// Sprawdzamy, czy paczka wymaga specjalnej obsługi
boolean requiresSpecialHandling = parcel.features()
    .get("fragile")
    .map(ProductFeatureInstance::asBoolean)
    .orElse(false);

// Pobieramy wagę do kalkulacji kosztów
BigDecimal weight = parcel.features()
    .get("weight")
    .map(ProductFeatureInstance::asDecimal)
    .orElseThrow(() -> new IllegalStateException("Weight is mandatory"));

// Sprawdzamy datę odbioru
LocalDate pickupDate = parcel.features()
    .get("pickupDate")
    .map(ProductFeatureInstance::asDate)
    .orElse(LocalDate.now());
```

**Co się tu dzieje?**
Logika biznesowa **nie zakłada**, że pola `fragile` czy `weight` istnieją w klasie. Pyta model: „czy ta paczka ma cechę X?". Jeśli ma, używa. Jeśli nie ma, stosuje domyślną wartość lub wyrzuca błąd.

To oznacza, że **jutro** możemy dodać nową cechę (np. „ubezpieczenie") i stary kod będzie działał bez zmian.

---

## Jednostki miar w cechach produktów

W wielu przypadkach cechy produktów mają jednostki miary: waga (kg), moc (HP), temperatura (°C), objętość (L).

### Podejście praktyczne: jednostka w nazwie cechy

W obecnej implementacji najprościej jest umieścić jednostkę w nazwie cechy:

```java
// Waga w kilogramach
ProductFeatureType weightKg =
    ProductFeatureType.withDecimalRange("weightKg", "0.1", "30.0");

// Moc silnika w koniach mechanicznych
ProductFeatureType enginePowerHP =
    ProductFeatureType.withNumericRange("enginePowerHP", 150, 250);

// Temperatura przechowywania w stopniach Celsjusza
ProductFeatureType storageTempC =
    ProductFeatureType.withNumericRange("storageTempC", -18, 25);

// Objętość w litrach
ProductFeatureType volumeL =
    ProductFeatureType.withDecimalRange("volumeL", "0.25", "5.0");
```

**Zalety:**
- Proste i czytelne
- Nie wymaga zmian w modelu
- Jednostka jest jawna w nazwie parametru

**Wady:**
- Jednostka jest "zakodowana" w string, trudno ją wyekstrahować programatowo
- Brak możliwości konwersji jednostek (np. kg ↔ lb)
- UI musi parsować nazwę, żeby wyświetlić "Waga (kg): 15.5"

### Możliwe rozszerzenie: Unit jako pole w ProductFeatureType

Dla bardziej zaawansowanych przypadków (konwersje jednostek, wielojęzyczność UI) można rozszerzyć model:

```java
class ProductFeatureType {
    private final String name;              // "enginePower"
    private final String displayName;       // "Moc silnika" (dla UI)
    private final Unit unit;                // Unit.of("HP") (opcjonalne)
    private final FeatureValueConstraint constraint;
}

// Użycie:
ProductFeatureType enginePower = ProductFeatureType.builder()
    .name("enginePower")
    .displayName("Moc silnika")
    .unit(Unit.of("HP"))
    .constraint(new NumericRangeConstraint(150, 250))
    .build();
```

**Korzyści tego podejścia:**
- Unit jest obiektem pierwszej klasy, można go programatowo przetwarzać
- UI automatycznie wie, jak wyświetlić: "Moc silnika: 200 HP"
- Możliwość konwersji (HP → kW, kg → lb, °C → °F)
- Cechy bez jednostki (kolor, materiał) mają `unit = null`

**Kiedy to ma sens:**
- System międzynarodowy z różnymi jednostkami w różnych krajach
- Potrzeba automatycznych konwersji
- Zaawansowane UI generowane z metadanych
- Integracje z systemami używającymi innych jednostek

Dla większości przypadków **wystarczy jednostka w nazwie**. Rozszerzenie o pole `Unit` można dodać później, gdy pojawi się rzeczywista potrzeba biznesowa.

---

## Podsumowanie: Korzyści z archetypów

### 1. Elastyczność bez zmian w kodzie

```java
// Dodajemy nową cechę produktu (np. „ekologiczny")
ProductFeatureType ecoFeature =
    ProductFeatureType.unconstrained("ecological", FeatureValueType.BOOLEAN);

// Dodajemy do istniejącego ProductType — bez zmiany kodu aplikacji
updatedType.builder(...)
    .withOptionalFeature(ecoFeature)
    .build();
```

**Korzyść:** Nowe cechy pojawiają się w systemie w ciągu minut, nie tygodni. Nie trzeba wdrażać nowej wersji aplikacji.

### 2. Spójność semantyczna

```java
// Wszędzie "rozmiar" to AllowedValuesConstraint
// Wszędzie "rok" to NumericRangeConstraint
// Wszędzie "kwota" to DecimalRangeConstraint
```

**Korzyść:** Nie ma „rozmiaru jako string w jednym miejscu i enum w drugim". Model jest spójny w całym systemie.

### 3. Walidacja w modelu, nie w logice

```java
// Zamiast:
if (size == null || !List.of("S","M","L","XL").contains(size)) {
    throw new IllegalArgumentException("Invalid size");
}

// Mamy:
ProductInstance instance = builder.withFeature(sizeFeature, "XXL").build();
// Rzuci wyjątek automatycznie, bo "XXL" nie jest na liście
```

**Korzyść:** Błędy są wyłapywane wcześniej. Logika biznesowa nie musi sprawdzać każdej cechy ręcznie.

### 4. Porównywanie i wyszukiwanie

```java
// Znajdź wszystkie koszulki w rozmiarze M
productInstances.stream()
    .filter(p -> p.features().get("size")
        .map(f -> f.asString().equals("M"))
        .orElse(false))
    .collect(toList());
```

**Korzyść:** Możemy budować wyszukiwarki, filtry, porównywarki produktów — bo model wie, jakie cechy są wspólne.

### 5. Dynamiczna konfiguracja

```java
// UI może dynamicznie wygenerować formularz na podstawie definicji
for (ProductFeatureType featureType : productType.featureTypes().allFeatures()) {
    if (featureType.constraint() instanceof AllowedValuesConstraint) {
        // Renderuj dropdown
    } else if (featureType.constraint() instanceof NumericRangeConstraint) {
        // Renderuj slider
    } else if (featureType.constraint() instanceof DateRangeConstraint) {
        // Renderuj date picker
    }
}
```

**Korzyść:** Interfejs użytkownika może być generowany automatycznie na podstawie modelu. Nie trzeba tworzyć osobnych formularzy dla każdego produktu.

---

## Wniosek

Cechy produktu to nie tylko opis. To **mechanizm różnicowania i konfigurowania** — czyli sposób, w jaki biznes steruje swoją ofertą.

Jeśli system nie potrafi odróżnić definicji produktu od jego konfiguracji, to:
- Każda nowa cecha staje się **zmianą programistyczną**
- Każdy nowy wariant staje się **mini-projektem IT**
- Organizacja **rozwijać ofertę w Excelu**, a nie w modelu

Archetypy `ProductFeatureType` i `ProductFeatureInstance` rozwiązują ten problem, pozwalając:
- Dodawać cechy dynamicznie (bez zmiany kodu)
- Walidować wartości w modelu (nie w logice biznesowej)
- Porównywać i wyszukiwać produkty (bo model jest spójny)
- Generować interfejsy automatycznie (bo definicje są dostępne w runtime)

To jest różnica między systemem, który **zmienia się tygodniami**, a systemem, który **dostosowuje się w minuty**.
