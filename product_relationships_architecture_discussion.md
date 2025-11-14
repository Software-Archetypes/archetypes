# Architektura relacji produktów - rozkminki

## Kluczowe pytania

Zanim zaimplementowaliśmy model, stanęliśmy przed fundamentalnymi wyborami:

**Czy relacje są częścią ProductType i zarządzane za jego pośrednictwem?**
A może relacje to niezależny byt?

**Czy powinniśmy zapewnić natychmiastową spójność reguł biznesowych dotyczących relacji?**
Czy jest to w ogóle możliwe? Jakie miałoby to konsekwencje?

**Czy jakiekolwiek zmiany na obiekcie ProductType powinny wpływać na relacje?**
Na przykład: czy możemy dodać relację do produktu, który został wycofany z oferty?

**Czy jakiekolwiek zmiany na relacjach powinny wpływać na ProductType?**
Na przykład: czy można wycofać produkt, który jest celem relacji UPGRADABLE_TO dla innych produktów?

## Opcja 1: Relacje jako część ProductType

Wyobraźmy sobie alternatywne podejście. Relacje żyją wewnątrz obiektu ProductType:

```java
class ProductType {
    private ProductIdentifier identifier;
    private ProductName name;
    private List<ProductRelationship> upgradesTo;
    private List<ProductRelationship> complementedBy;
    private List<ProductRelationship> incompatibleWith;
    // ...

    public void addUpgrade(ProductType target) {
        // logika walidacji
        // dodanie relacji
    }
}
```

### Plusy tego podejścia

**Prostota architektury.** Nie potrzebujesz oddzielnej tabeli relacji, oddzielnego repozytorium, oddzielnej fasady. Wszystko w jednym miejscu. Początkowy rozwój jest szybszy.

**Naturalne powiązanie z domeną.** Relacje są z natury związane z produktem, do którego należą. ProductType "Burger" naturalnie przechowuje swoje dodatki, swoje upgrade'y, swoje wykluczenia. To odpowiada intuicyjnemu modelowi domeny.

**Wydajność dla lokalnych operacji.** Pobieranie relacji pojedynczego produktu może być szybsze, bo dane są już zlokalizowane. Jeden load obiektu ProductType i masz wszystko.

**Atomowość zmian.** Jeśli relacje są częścią agregatu ProductType, możesz je zmieniać atomowo razem z produktem. Jedna transakcja, jedna granica spójności.

**Brak osieroconych relacji.** Powiązanie relacji z produktem zapewnia kontekst i zmniejsza ryzyko wystąpienia relacji wskazujących na nieistniejące produkty.

### Minusy tego podejścia

**Model ProductType staje się "obiektem Boga".** Łączysz dane produktu z zarządzaniem wszystkimi jego relacjami. Obiekt rośnie, komplikuje się. Testowanie, utrzymanie, integracja stają się trudniejsze. Naruszasz Single Responsibility Principle.

**Problem ze skalą.** Co jeśli produkt ma 500 dodatków? 1000 produktów kompatybilnych? Burger w międzynarodowej sieci może mieć dziesiątki dodatków w różnych regionach. Adapter do wózka może być kompatybilny z setkami modeli. Ładujesz ProductType – ładujesz wszystkie relacje. Przeciążasz obiekt.

**Wydajność dla operacji skupionych na relacjach.** Chcesz znaleźć wszystkie produkty, które są UPGRADABLE_TO do produktu X? Musisz przeszukać wszystkie produkty, załadować je, sprawdzić ich kolekcje relacji. Nie masz indeksów na `to`.

**Egzekwowanie reguł biznesowych staje się trudniejsze.** Masz regułę: "produkt nie może być kompatybilny sam ze sobą". Łatwo sprawdzić w ProductType. Ale masz regułę: "jeśli A jest UPGRADABLE_TO B, to B nie może być UPGRADABLE_TO A" (cykliczne upgrade'y). Musisz załadować ProductType B, sprawdzić jego relacje. Potrzebujesz dostępu do innych obiektów. Logika wycieka poza agregat.

**Blokowanie i dostępność.** Żeby zapewnić spójność, musisz blokować. Dodajesz relację COMPATIBLE_WITH z adaptera do wózka? Musisz zablokować adapter. A co jeśli chcesz pilnować, że wózek nie ma już więcej niż 100 kompatybilnych akcesoriów? Musisz zablokować też wózek. Pesymistyczne blokowanie dwóch obiektów. Dostępność spada.

**Trudność w rozbudowie.** Nowy typ relacji? Nowa kolekcja w ProductType. Nowa metoda. Nowa logika walidacji. Zmieniasz centralny obiekt domeny za każdym razem.

## Opcja 2: Relacje jako niezależny byt (nasz wybór)

Relacje żyją osobno. Mają własne repozytorium, własne queries, własną fasadę.

```java
// ProductType pozostaje prosty
class ProductType {
    private ProductIdentifier identifier;
    private ProductName name;
    private ProductDescription description;
    // Brak relacji
}

// Relacje zarządzane osobno
class ProductRelationshipsFacade {
    Result<String, ProductRelationship> defineRelationship(
        ProductIdentifier from,
        ProductIdentifier to,
        ProductRelationshipType type
    );
}
```

### Plusy tego podejścia

**Separacja odpowiedzialności.** ProductType zajmuje się produktem. ProductRelationshipsFacade zajmuje się relacjami. Jasne granice, łatwiejsze testowanie, łatwiejsze utrzymanie.

**Skalowalność.** Produkt może mieć tysiące relacji – nie wpływa to na rozmiar obiektu ProductType. Relacje żyją w osobnej tabeli, mają własne indeksy. Szukanie po `from`, po `to`, po `type` – szybkie i efektywne.

**Elastyczność w egzekwowaniu reguł.** Policy działają na poziomie relacji, nie produktu. Możesz łatwo sprawdzić, czy już istnieje relacja odwrotna, czy produkt ma już za dużo relacji danego typu. Logika jest w jednym miejscu.

**Brak blokowania produktów.** Dodajesz relację? Nie blokujesz ProductType. Relacja to osobny wiersz w bazie, osobny obiekt. Możesz dodawać relacje równolegle, niezależnie od operacji na produktach.

**Łatwość rozbudowy.** Nowy typ relacji? Dodajesz wartość do enuma. Policy obsługuje nowy typ. ProductType nawet nie wie, że coś się zmieniło.

**Optymalizacja zapytań.** Chcesz wszystkie produkty COMPLEMENTED_BY dla burgera? Jedno zapytanie: `SELECT * FROM relationships WHERE from = burger AND type = COMPLEMENTED_BY`. Szybkie. Indeksowane.

### Minusy tego podejścia

**Większa złożoność architektury.** Potrzebujesz dodatkowej tabeli, dodatkowego repozytorium, dodatkowej fasady. Więcej kodu do napisania i utrzymania.

**Brak atomowości między produktem a relacjami.** ProductType i jego relacje to osobne obiekty, osobne transakcje. Mogą się rozsynchronizować. Usunąłeś produkt, ale relacje zostały? Możliwe. Potrzebujesz dodatkowej logiki (cascade delete) albo eventual consistency.

**Trudniejsze zapewnienie spójności referencyjnej.** Relacja wskazuje na ProductIdentifier. Czy ten produkt istnieje? Musisz sprawdzić w fasadzie (tak jak robimy). Nie masz gwarancji foreign key na poziomie agregatu.

**Potencjalne osierocone relacje.** Jeśli nie posprzątasz po usunięciu produktu, relacje mogą wskazywać w próżnię. Potrzebujesz procesów czyszczących albo eventual consistency.

## Co zatem zrobić gdy chcemy mieć większą skalę, dużą elastyczność?

Widzimy, że próba zapewnienia spójności natychmiastowej między produktami a relacjami jest bardzo kosztowna.

### Spójność natychmiastowa - konsekwencje

Żeby zagwarantować, że każda relacja wskazuje na istniejące produkty, musielibyśmy:

1. **Blokować ProductType przy dodawaniu relacji.** Pesymistyczne blokowanie. Niższa dostępność.

2. **Blokować oba produkty (from i to) przy dodawaniu relacji z regułą dwustronną.** Np. kompatybilność – możesz chcieć pilnować, że wózek nie ma więcej niż 100 kompatybilnych akcesoriów. Blokujesz adapter i wózek. Dwa obiekty jednocześnie. Deadlocki czają się za rogiem.

3. **Blokować wszystkie relacje przy usuwaniu produktu.** Żeby żadna relacja nie wskazywała na usuwany produkt, musisz je znaleźć i usunąć. Albo zablokować możliwość usunięcia produktu, który ma relacje. Sztywność.

To wszystko obniża dostępność systemu. Im więcej blokad, tym mniej concurrent operacji, tym niższa przepustowość.

Ponadto, model ten i tak wbrew pozorom nie wspiera pełnej spójności, gdyż możemy mieć reguły które wymagać będą blokowania większej liczby podmiotów – to musiałoby się wykonać już na warstwie aplikacyjnej, co jeszcze bardziej komplikuje sprawy.

### Zadajmy sobie pytanie: jakie będą konsekwencje tego, że burger będzie mieć 11 a nie 10 dodatków?

Albo: co się stanie, jeśli relacja COMPATIBLE_WITH wskazuje na produkt, który w międzyczasie został wycofany?

**Może te sytuacje są na tyle rzadkie, że złamanie reguły biznesowej ma bardzo mały wpływ i jest akceptowalne?**

**Może asynchroniczna rekonsyliacja będzie wystarczająca?**

Biorąc pod uwagę nasze doświadczenie i badania: **skalowalność, elastyczność i dostępność są często dużo ważniejsze niż spójność natychmiastowa**, zwłaszcza gdy zarządzanie produktami i ich relacjami to procesy, które są z założenia rozdzielone w czasie.

Manager produktu definiuje relacje w backoffice. Klient widzi je w sklepie godzinę później. Nie potrzebujesz atomowości. Potrzebujesz eventual consistency.

## Relacje zamodelowane obok Product - konsekwencje

### Ten model jest dobry dla

**Systemów o dużej skali.** Tysiące produktów, dziesiątki tysięcy relacji. E-commerce, marketplace, katalogi przemysłowe.

**Scenariuszy, w których relacje są często zmieniane niezależnie od produktów.** Manager produktu dodaje kompatybilności, definiuje cross-selling, aktualizuje upgrade path – niezależnie od zmian w samych produktach.

**Środowisk, gdzie dostępność i przepustowość są ważniejsze niż spójność natychmiastowa.** Sklep online musi działać 24/7. Lepiej pokazać klientowi nieaktualną rekomendację niż zawiesić się przez blokadę.

**Systemów, gdzie relacje to metadata sprzedażowa, nie krytyczna logika biznesowa.** Cross-selling, up-selling to rekomendacje. Jeśli są chwilowo nieaktualne, świat się nie zawali. W przeciwieństwie do twardych reguł jak "karta kredytowa wymaga konta" – tu eventual consistency może nie wystarczyć.

### Minusy

**Konieczność eventual consistency.** Musisz zaakceptować, że relacje mogą być chwilowo niespójne. Produkt usunięty, relacja jeszcze istnieje. Potrzebujesz procesów asynchronicznych do sprzątania.

**Większa złożoność developerska.** Dwa moduły, dwie fasady, integracja między nimi. Więcej kodu, więcej testów.

**Trudniejsze reasoning o stanie systemu.** Stan produktu i stan jego relacji to dwa osobne wymiary. Nie możesz powiedzieć "załadowałem ProductType, wiem wszystko". Musisz dodatkowo zapytać o relacje.

## W związku z tym proponujemy następujący model

**Relacje to niezależny byt.** ProductRelationship żyje osobno, ma własne ID, własne repozytorium.

**Oddzielna fasada.** ProductRelationshipsFacade zarządza relacjami. ProductFacade nie wie o relacjach. Separacja odpowiedzialności.

**Eventual consistency.** Sprawdzamy, czy produkty istnieją przy dodawaniu relacji (fail-fast dla oczywistych błędów), ale nie blokujemy produktów. Jeśli produkt zostanie usunięty później, relacje posprzątamy asynchronicznie.

**Policy jako punkt rozszerzenia.** Reguły biznesowe dotyczące relacji żyją w policy. Chcesz ograniczyć liczbę dodatków? Piszesz MaxComplementsPolicy. Chcesz zabronić cyklicznych upgrade'ów? NoCyclicUpgradePolicy. Nie zmieniasz ProductType ani fasady.

**Queries zoptymalizowane pod relacje.** `findAllRelationsFrom`, `findAllRelationsFrom(productId, type)`, `findMatching(predicate)`. Szybkie szukanie po `from`, po `to`, po `type`.

**Brak blokowania produktów.** Dodawanie relacji nie blokuje ProductType. Możesz równolegle dodawać relacje dla tego samego produktu. Wysoka dostępność.

## Kiedy byś wybrał opcję 1 (relacje w ProductType)?

**Systemów z niewielką lub zarządzalną liczbą relacji.** Prosta aplikacja z kilkudziesięcioma produktami, każdy ma 2-3 relacje. Prostota architektury przeważa nad skalowalnością.

**Scenariuszy, w których główny nacisk kładziony jest na pojedyncze produkty i ich bezpośrednie połączenia.** Czytasz ProductType, natychmiast masz wszystkie jego relacje. Brak dodatkowych zapytań.

**Środowisk, w których prostota i szybki rozwój mają większe znaczenie niż skalowalność lub modularność.** Startup, MVP, proof of concept. Jedna klasa, jeden agregat, szybko działa.

**Systemów, gdzie relacje są krytyczne dla spójności biznesowej i wymagają atomowości.** Np. produkt typu "pakiet" wymaga, żeby wszystkie jego komponenty były dostępne jednocześnie. Relacje nie mogą być niespójne nawet chwilowo.

## Podsumowanie

Wybraliśmy relacje jako niezależny byt, ponieważ:

1. **Skalowalność jest kluczowa.** Produkty mogą mieć setki relacji. Nie chcemy przeciążać obiektu ProductType.

2. **Elastyczność jest ważniejsza niż spójność natychmiastowa.** Eventual consistency wystarcza dla metadata sprzedażowej.

3. **Dostępność przeważa nad blokowaniem.** Nie chcemy blokować produktów przy operacjach na relacjach.

4. **Separacja odpowiedzialności ułatwia rozwój.** ProductType zajmuje się produktem. ProductRelationshipsFacade zajmuje się relacjami. Jasne granice.

To trade-off. Płacimy większą złożonością architektury za skalowalność i elastyczność. Ale dla systemów produktowych o większej skali – to właściwa cena.
