# PartyRole - Porównanie Trzech Podejść Modelowania

## Wprowadzenie

W archetype Party jednym z kluczowych wyzwań jest modelowanie ról, jakie może pełnić Party (osoba lub organizacja). Istnieją trzy główne podejścia do tego problemu:

1. **PartyRole z tekstową wartością (String)**
2. **PartyRole jako hierarchia dziedziczenia**
3. **PartyRoleType - wzorzec Type-Instance**

Każde z tych podejść odpowiada na inne potrzeby biznesowe i architektoniczne. Kluczowe pytania, które determinują wybór podejścia to:
- Kto zarządza katalogiem ról? (programista vs administrator biznesowy)
- Jak często pojawiają się nowe role? (rzadko vs często)
- Czy role mają dodatkowe atrybuty wspólne dla wszystkich instancji? (nie vs tak)
- Jak ważna jest kontrola typów? (runtime vs compile-time)

---

## Podejście 1: PartyRole z Tekstową Wartością (String)

### Storytelling: Start-up w Trybie MVP

Wyobraź sobie młody start-up budujący platformę do zarządzania freelancerami. Zespół składa się z 3 deweloperów, mają 6 tygodni na MVP.

**Potrzeby**:
- Szybka implementacja podstawowych ról: CLIENT, FREELANCER, ADMIN
- Nie ma czasu na budowanie zaawansowanych mechanizmów
- Role są proste, nie mają dodatkowych atrybutów
- Zmiany w rolach są rzadkie (raz na kilka miesięcy)

**Rozwiązanie**: Proste role jako String

### Model i Implementacja

```java
public record PartyRole(PartyId partyId, Role role, Validity validity) {

    public static PartyRole assignRole(PartyId partyId, String roleName) {
        return new PartyRole(partyId, Role.of(roleName), Validity.forever());
    }

    public boolean hasRole(String roleName) {
        return role.name().equalsIgnoreCase(roleName);
    }
}

public record Role(String name) {

    public static Role of(String name) {
        return new Role(name.toUpperCase());
    }
}
```

### Przykład użycia w aplikacji

```java
// Rejestracja klienta
@PostMapping("/api/register/client")
public void registerClient(@RequestBody RegisterClientRequest request) {
    PartyId clientId = partiesFacade.registerPerson(
        request.firstName(),
        request.lastName(),
        request.email()
    );

    // Przypisanie roli - po prostu String
    partyRolesFacade.assignRole(clientId, "CLIENT");
}

// Rejestracja freelancera
@PostMapping("/api/register/freelancer")
public void registerFreelancer(@RequestBody RegisterFreelancerRequest request) {
    PartyId freelancerId = partiesFacade.registerPerson(
        request.firstName(),
        request.lastName(),
        request.email()
    );

    partyRolesFacade.assignRole(freelancerId, "FREELANCER");
}

// Sprawdzenie uprawnień
@PostMapping("/api/projects")
public void createProject(@RequestBody CreateProjectRequest request, @AuthUser PartyId userId) {
    // Tylko klient może tworzyć projekty
    if (!partyRolesFacade.hasRole(userId, "CLIENT")) {
        throw new UnauthorizedException("Only clients can create projects");
    }

    projectService.createProject(userId, request);
}
```

### Scenariusz biznesowy w praktyce

```java
// Dzień 1: Wdrożenie MVP
PartyRole john = PartyRole.assignRole(johnId, "CLIENT");
PartyRole anna = PartyRole.assignRole(annaId, "FREELANCER");
PartyRole admin = PartyRole.assignRole(adminId, "ADMIN");

// Tydzień 3: Nowa potrzeba biznesowa - moderatorzy
PartyRole moderator = PartyRole.assignRole(kateId, "MODERATOR");
// ✓ Dodane w 5 sekund, bez zmian w kodzie

// Miesiąc 2: Problem - literówki i niespójności
PartyRole bob1 = PartyRole.assignRole(bobId, "CLIENT");
PartyRole bob2 = PartyRole.assignRole(bobId, "client");    // ✗ Duplikat?
PartyRole bob3 = PartyRole.assignRole(bobId, "Klient");    // ✗ Po polsku?

// Problem z raportowaniem
int clientCount = partyRoleRepository.countByRoleName("CLIENT");
// ✗ Nie zliczy "client" ani "Klient"
```

### Zalety
✓ **Szybka implementacja** - gotowe w godzinę
✓ **Elastyczność** - dowolny String, łatwe dodawanie ról
✓ **Brak dodatkowej infrastruktury** - nie trzeba zarządzać katalogiem
✓ **Idealne dla MVP** - wystarczy na start

### Wady
✗ **Brak kontroli nad wartościami** - literówki, duplikaty, niespójności
✗ **Brak walidacji** - można przypisać nieistniejącą rolę
✗ **Trudne raportowanie** - ile różnych ról mamy w systemie?
✗ **Brak miejsca na metadane** - nie można dodać opisu, ograniczeń, etc.
✗ **Trudna ewolucja** - ciężko przenieść później na bardziej zaawansowane rozwiązanie

### Kiedy używać?
- **MVP i prototypy** - szybkie sprawdzenie pomysłu
- **Proste systemy** - 3-5 ról, rzadko się zmieniających
- **Małe zespoły** - gdzie wszyscy wiedzą jakie role istnieją
- **Krótki czas życia projektu** - nie będzie ewoluował

---

## Podejście 2: PartyRole jako Hierarchia Dziedziczenia

### Storytelling: Aplikacja Bankowa z Rygorystycznymi Wymaganiami

Bank buduje system do obsługi kredytów hipotecznych. Wymogi regulacyjne są bardzo sztywne:
- Tylko oficer kredytowy może zatwierdzać kredyty do 500k PLN
- Tylko senior oficer może zatwierdzać kredyty powyżej 500k PLN
- Tylko compliance officer może przeprowadzać audyty
- Każda rola ma specyficzne zachowania wymagane przez audytorów

**Potrzeby**:
- **Type-safety** - błąd w przypisaniu roli może kosztować miliony
- **Kontrola kompilacyjna** - audytorzy wymagają pewności na poziomie kodu
- **Specyficzne zachowania** - każda rola ma swoje unikalne metody
- **Stabilność** - nowe role pojawiają się raz na 2-3 lata
- **Dokumentacja kodu** - każda rola musi być klarownie zdefiniowana

**Rozwiązanie**: Hierarchia klas z type-safety

### Model i Implementacja

```java
// Abstrakcyjna klasa bazowa
public abstract class PartyRole {
    protected final PartyId partyId;
    protected final Validity validity;

    protected PartyRole(PartyId partyId, Validity validity) {
        this.partyId = partyId;
        this.validity = validity;
    }

    public PartyId partyId() {
        return partyId;
    }

    public abstract String roleName();

    public abstract boolean canPerformAction(String action);
}

// Konkretne role z business logic
public final class LoanOfficer extends PartyRole {
    private static final Money MAX_APPROVAL_LIMIT = Money.of(500_000, "PLN");

    public LoanOfficer(PartyId partyId, Validity validity) {
        super(partyId, validity);
    }

    @Override
    public String roleName() {
        return "LOAN_OFFICER";
    }

    @Override
    public boolean canPerformAction(String action) {
        return switch (action) {
            case "APPROVE_LOAN", "REVIEW_APPLICATION", "REQUEST_DOCUMENTS" -> true;
            default -> false;
        };
    }

    // Specyficzna logika biznesowa
    public boolean canApprove(Money loanAmount) {
        return loanAmount.isLessThanOrEqualTo(MAX_APPROVAL_LIMIT);
    }

    public LoanApprovalResult approveLoan(LoanApplication application) {
        if (!canApprove(application.amount())) {
            return LoanApprovalResult.requiresSeniorApproval(
                "Loan amount exceeds officer limit"
            );
        }
        // ... logika zatwierdzania
        return LoanApprovalResult.approved();
    }
}

public final class SeniorLoanOfficer extends PartyRole {

    public SeniorLoanOfficer(PartyId partyId, Validity validity) {
        super(partyId, validity);
    }

    @Override
    public String roleName() {
        return "SENIOR_LOAN_OFFICER";
    }

    @Override
    public boolean canPerformAction(String action) {
        return switch (action) {
            case "APPROVE_LOAN", "APPROVE_HIGH_VALUE_LOAN",
                 "REVIEW_APPLICATION", "REQUEST_DOCUMENTS",
                 "OVERRIDE_DECISION" -> true;
            default -> false;
        };
    }

    // Brak limitu zatwierdzeń
    public boolean canApprove(Money loanAmount) {
        return true; // No limit for senior officers
    }

    public LoanApprovalResult approveLoan(LoanApplication application) {
        // ... zaawansowana logika dla dużych kredytów
        return LoanApprovalResult.approved();
    }
}

public final class ComplianceOfficer extends PartyRole {

    public ComplianceOfficer(PartyId partyId, Validity validity) {
        super(partyId, validity);
    }

    @Override
    public String roleName() {
        return "COMPLIANCE_OFFICER";
    }

    @Override
    public boolean canPerformAction(String action) {
        return switch (action) {
            case "AUDIT_LOAN", "REVIEW_COMPLIANCE", "GENERATE_REPORT" -> true;
            default -> false;
        };
    }

    // Specyficzna logika audytu
    public ComplianceAuditResult auditLoan(LoanId loanId) {
        // ... logika audytu zgodności
        return ComplianceAuditResult.compliant();
    }
}
```

### Przykład użycia w aplikacji

```java
// Service layer - type-safety gwarantuje poprawność
public class LoanApprovalService {

    // Kompilator wymusza przekazanie LoanOfficer, nie String
    public LoanApprovalResult submitForApproval(
        LoanApplication application,
        LoanOfficer officer  // ✓ Type-safe!
    ) {
        // Kompilator wie, że LoanOfficer ma metodę canApprove
        if (!officer.canApprove(application.amount())) {
            return LoanApprovalResult.requiresSeniorApproval(
                "Amount exceeds officer limit: " + application.amount()
            );
        }

        return officer.approveLoan(application);
    }

    // Osobna metoda dla senior officer - nie można pomylić!
    public LoanApprovalResult submitForSeniorApproval(
        LoanApplication application,
        SeniorLoanOfficer seniorOfficer  // ✓ Type-safe!
    ) {
        return seniorOfficer.approveLoan(application);
    }
}

// Controller
@PostMapping("/api/loans/{loanId}/approve")
public ResponseEntity<?> approveLoan(
    @PathVariable LoanId loanId,
    @AuthUser PartyId officerId
) {
    LoanApplication application = loanRepository.findById(loanId);

    // Sprawdzenie typu roli
    PartyRole role = partyRoleRepository.findActiveRole(officerId);

    return switch (role) {
        case LoanOfficer officer -> {
            LoanApprovalResult result = loanApprovalService
                .submitForApproval(application, officer);
            yield ResponseEntity.ok(result);
        }
        case SeniorLoanOfficer seniorOfficer -> {
            LoanApprovalResult result = loanApprovalService
                .submitForSeniorApproval(application, seniorOfficer);
            yield ResponseEntity.ok(result);
        }
        default -> ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body("Only loan officers can approve loans");
    };
}
```

### Scenariusz biznesowy w praktyce

```java
// Przypisanie ról - type-safe
LoanOfficer johnOfficer = new LoanOfficer(johnId, Validity.forever());
SeniorLoanOfficer aliceSenior = new SeniorLoanOfficer(aliceId, Validity.forever());

// Zatwierdzanie kredytu 300k PLN
LoanApplication loan300k = new LoanApplication(Money.of(300_000, "PLN"));

// ✓ John może zatwierdzić
LoanApprovalResult result1 = johnOfficer.approveLoan(loan300k);
// result1 = APPROVED

// Zatwierdzanie kredytu 700k PLN
LoanApplication loan700k = new LoanApplication(Money.of(700_000, "PLN"));

// ✗ John NIE może zatwierdzić
LoanApprovalResult result2 = johnOfficer.approveLoan(loan700k);
// result2 = REQUIRES_SENIOR_APPROVAL ("Loan amount exceeds officer limit")

// ✓ Alice może zatwierdzić
LoanApprovalResult result3 = aliceSenior.approveLoan(loan700k);
// result3 = APPROVED

// Kompilator nie pozwoli na błędy
// loanApprovalService.submitForApproval(loan700k, aliceSenior);
// ✗ COMPILATION ERROR: Expected LoanOfficer, got SeniorLoanOfficer
```

### Zalety
✓ **Type-safety na poziomie kompilacji** - niemożliwe błędy typu "CLIENT zatwierdzający kredyt"
✓ **IDE support** - autocomplete, refactoring, find usages
✓ **Specyficzne zachowania** - każda rola może mieć swoje metody
✓ **Klarowna dokumentacja** - kod jest self-documenting
✓ **Bezpieczeństwo** - audytorzy widzą wszystkie role w kodzie
✓ **Pattern matching** - switch expression zapewnia exhaustiveness checking

### Wady
✗ **Brak elastyczności** - nowa rola = nowa klasa + kompilacja + deployment
✗ **Eksplozja klas** - 20 ról = 20 klas
✗ **Niemożliwość konfiguracji runtime** - nie można dodać roli przez API
✗ **Trudne multi-tenancy** - każdy tenant musi mieć te same role
✗ **Overhead w prostych przypadkach** - jeśli role są tylko "etykietkami"

### Kiedy używać?
- **Systemy regulowane** - bankowość, medycyna, finanse
- **Krytyczne bezpieczeństwo** - gdzie błąd w roli to duże ryzyko
- **Stabilny katalog ról** - role zmieniają się rzadko (raz na lata)
- **Specyficzne zachowania** - każda rola ma swoje unikalne metody
- **Type-safety requirement** - kompilator musi wykrywać błędy

---

## Podejście 3: PartyRoleType - Wzorzec Type-Instance

### Storytelling: SaaS dla Firm Konsultingowych (Multi-Tenant)

Budujesz platformę SaaS dla firm konsultingowych. Masz już 50 klientów, każdy ma swoje unikalne role:
- **Firma A**: Junior Consultant, Senior Consultant, Partner, Practice Lead
- **Firma B**: Analyst, Consultant, Manager, Director, Managing Director
- **Firma C**: Trainee, Associate, VP, SVP, MD

**Potrzeby**:
- **Multi-tenancy** - każdy klient definiuje swoje role
- **Self-service** - klienci sami zarządzają swoim katalogiem ról przez UI
- **Częste zmiany** - klienci dodają nowe role co tydzień
- **Rich metadata** - każda rola ma opis, wymagania, ścieżkę kariery
- **Reużywalność** - niektóre role są podobne między klientami
- **Raportowanie** - "ile osób ma rolę X?", "jakie role istnieją?"

**Rozwiązanie**: Wzorzec Type-Instance (jak ProductType/ProductInstance)

### Model i Implementacja

```java
// PartyRoleType - KATALOG (template dla roli)
public record PartyRoleType(
    PartyRoleTypeId id,
    TenantId tenantId,           // ✓ Multi-tenant support
    String name,
    String description,
    Set<PartyRoleConstraint> constraints
) {

    public static PartyRoleType define(
        TenantId tenantId,
        String name,
        String description,
        PartyRoleConstraint... constraints
    ) {
        // Walidacja nazwy
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        return new PartyRoleType(
            PartyRoleTypeId.newOne(),
            tenantId,
            name,
            description,
            Set.of(constraints)
        );
    }

    public boolean canBeAssignedTo(Party party) {
        return constraints.stream()
            .allMatch(constraint -> constraint.isSatisfiedBy(party));
    }
}

// PartyRole - INSTANCJA (konkretne przypisanie roli)
public record PartyRole(
    PartyRoleId id,
    PartyId partyId,
    PartyRoleType roleType,      // ✓ Referencja do katalogu
    Validity validity
) {

    public static PartyRole assign(
        PartyId partyId,
        PartyRoleType roleType,
        Validity validity,
        Party party
    ) {
        // Walidacja constraints z katalogu
        if (!roleType.canBeAssignedTo(party)) {
            throw new RoleAssignmentConstraintViolation(
                "Party does not satisfy constraints for role: " + roleType.name()
            );
        }

        return new PartyRole(
            PartyRoleId.newOne(),
            partyId,
            roleType,
            validity
        );
    }

    public String roleName() {
        return roleType.name();
    }

    public boolean isActive() {
        return validity.isActiveAt(LocalDate.now());
    }
}

// Constraints przykłady
public interface PartyRoleConstraint {
    boolean isSatisfiedBy(Party party);
    String description();

    static PartyRoleConstraint onlyPerson() {
        return new OnlyPersonConstraint();
    }

    static PartyRoleConstraint onlyOrganization() {
        return new OnlyOrganizationConstraint();
    }

    static PartyRoleConstraint minimumAge(int age) {
        return new MinimumAgeConstraint(age);
    }
}
```

### Przykład użycia - Zarządzanie katalogiem (Admin UI)

```java
// REST API dla administratora firmy konsultingowej
@RestController
@RequestMapping("/api/tenants/{tenantId}/role-types")
public class RoleTypeAdminController {

    // Administrator Firmy A definiuje swoje role
    @PostMapping
    public PartyRoleType createRoleType(
        @PathVariable TenantId tenantId,
        @RequestBody CreateRoleTypeRequest request
    ) {
        PartyRoleType juniorConsultant = PartyRoleType.define(
            tenantId,
            "Junior Consultant",
            "Entry-level consultant, works under supervision of senior consultants",
            PartyRoleConstraint.onlyPerson(),
            PartyRoleConstraint.minimumAge(21)
        );

        return roleTypeRepository.save(juniorConsultant);
    }

    // Administrator może edytować opis roli
    @PutMapping("/{roleTypeId}")
    public PartyRoleType updateRoleType(
        @PathVariable TenantId tenantId,
        @PathVariable PartyRoleTypeId roleTypeId,
        @RequestBody UpdateRoleTypeRequest request
    ) {
        PartyRoleType existing = roleTypeRepository
            .findByIdAndTenantId(roleTypeId, tenantId);

        PartyRoleType updated = new PartyRoleType(
            existing.id(),
            existing.tenantId(),
            request.name(),           // ✓ Zmiana nazwy
            request.description(),    // ✓ Zmiana opisu
            existing.constraints()
        );

        roleTypeRepository.save(updated);

        // ✓ WSZYSTKIE istniejące PartyRole automatycznie mają nową nazwę!
        return updated;
    }

    // Lista wszystkich ról w firmie
    @GetMapping
    public List<PartyRoleType> listRoleTypes(@PathVariable TenantId tenantId) {
        return roleTypeRepository.findByTenantId(tenantId);
    }
}
```

### Przykład użycia - Przypisywanie ról (HR Manager UI)

```java
// HR Manager przypisuje role pracownikom
@RestController
@RequestMapping("/api/tenants/{tenantId}/employees/{employeeId}/roles")
public class EmployeeRoleController {

    @PostMapping
    public PartyRole assignRole(
        @PathVariable TenantId tenantId,
        @PathVariable PartyId employeeId,
        @RequestBody AssignRoleRequest request
    ) {
        // Pobranie roli z katalogu firmy
        PartyRoleType roleType = roleTypeRepository
            .findByIdAndTenantId(request.roleTypeId(), tenantId);

        Party employee = partyRepository.findById(employeeId);

        // Przypisanie roli z walidacją constraints
        PartyRole role = PartyRole.assign(
            employeeId,
            roleType,
            Validity.from(LocalDate.now()),
            employee
        );

        return partyRoleRepository.save(role);
    }

    // Historia ról pracownika
    @GetMapping
    public List<PartyRole> getEmployeeRoles(
        @PathVariable TenantId tenantId,
        @PathVariable PartyId employeeId
    ) {
        return partyRoleRepository.findByPartyId(employeeId);
    }
}
```

### Scenariusz biznesowy w praktyce

```java
// === FIRMA A (Tenant 1) ===

// Administrator definiuje katalog ról (raz, przez UI)
PartyRoleType juniorConsultant = PartyRoleType.define(
    firmAId,
    "Junior Consultant",
    "Entry-level consultant",
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(21)
);

PartyRoleType seniorConsultant = PartyRoleType.define(
    firmAId,
    "Senior Consultant",
    "Experienced consultant, leads projects",
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(25)
);

PartyRoleType partner = PartyRoleType.define(
    firmAId,
    "Partner",
    "Firm partner, owns equity",
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(35)
);

// HR Manager przypisuje role pracownikom (wiele razy)
PartyRole john = PartyRole.assign(johnId, juniorConsultant, Validity.forever(), johnParty);
PartyRole anna = PartyRole.assign(annaId, seniorConsultant, Validity.forever(), annaParty);
PartyRole mike = PartyRole.assign(mikeId, partner, Validity.forever(), mikeParty);
// ... 200 more employees

// === FIRMA B (Tenant 2) - ZUPEŁNIE INNE ROLE ===

PartyRoleType analyst = PartyRoleType.define(
    firmBId,
    "Analyst",
    "Junior analyst role",
    PartyRoleConstraint.onlyPerson()
);

PartyRoleType director = PartyRoleType.define(
    firmBId,
    "Director",
    "Senior leadership role",
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(30)
);

PartyRole bob = PartyRole.assign(bobId, analyst, Validity.forever(), bobParty);
PartyRole kate = PartyRole.assign(kateId, director, Validity.forever(), kateParty);

// === RAPORTOWANIE ===

// Firma A: Ile mamy Junior Consultants?
long juniorCount = partyRoleRepository.countByRoleType(juniorConsultant);
// 45 osób

// Firma B: Jakie role istnieją?
List<PartyRoleType> firmBRoles = roleTypeRepository.findByTenantId(firmBId);
// ["Analyst", "Consultant", "Manager", "Director", "Managing Director"]

// === EWOLUCJA ===

// Miesiąc później: Firma A dodaje nową rolę (przez UI, bez kodu!)
PartyRoleType practiceLead = PartyRoleType.define(
    firmAId,
    "Practice Lead",
    "Leads specific practice area (e.g., Strategy, Operations)",
    PartyRoleConstraint.onlyPerson(),
    PartyRoleConstraint.minimumAge(30)
);
// ✓ Dodane w 30 sekund przez admina, bez deploy

// Rok później: Firma A zmienia nazwę roli
PartyRoleType updated = new PartyRoleType(
    seniorConsultant.id(),
    seniorConsultant.tenantId(),
    "Senior Consultant (Manager Level)",  // ✓ Nowa nazwa
    "Experienced consultant, leads projects and manages team",
    seniorConsultant.constraints()
);
roleTypeRepository.save(updated);
// ✓ Wszystkie 60 osób z tą rolą automatycznie mają nową nazwę
```

### Rozszerzenia (opcjonalne)

PartyRoleType można łatwo rozszerzyć o dodatkowe elementy:

```java
public record PartyRoleType(
    PartyRoleTypeId id,
    TenantId tenantId,
    String name,
    String description,
    Set<PartyRoleConstraint> constraints,

    // Opcjonalne rozszerzenia:
    Set<Responsibility> responsibilities,     // Co może robić osoba w tej roli?
    Set<Capability> capabilities,             // Jakie umiejętności są wymagane?
    CareerPath nextRoles,                     // Jaka jest ścieżka awansu?
    SalaryRange salaryRange                   // Jaki jest przedział wynagrodzenia?
) {
    // ... wszystkie dane przechowywane RAZ w typie,
    //     nie duplikowane w każdej instancji
}
```

### Zalety
✓ **Multi-tenancy native** - każdy klient ma swój katalog ról
✓ **Self-service** - klienci zarządzają rolami przez UI, bez kodu
✓ **Runtime flexibility** - nowe role dodawane on-demand
✓ **Rich metadata** - katalog przechowuje opisy, constraints, etc.
✓ **Centralne zarządzanie** - jedna zmiana w katalogu = update wszędzie
✓ **Łatwe raportowanie** - "ile osób ma rolę X?", "jakie role mamy?"
✓ **Analogia do ProductType** - znany wzorzec, łatwy do zrozumienia
✓ **Skalowalność** - 1000 pracowników z rolą X = 1 definicja + 1000 referencji

### Wady
✗ **Większa złożoność** - dwie encje (Type + Instance) zamiast jednej
✗ **Wymaga UI do zarządzania** - admini potrzebują interfejsu
✗ **Brak type-safety** - błędy wykrywane w runtime, nie compile-time
✗ **Overhead w prostych przypadkach** - jeśli role są statyczne i proste

### Kiedy używać?
- **SaaS / Multi-tenant** - każdy klient ma swoje role
- **Self-service platforms** - użytkownicy sami zarządzają konfiguracją
- **Częste zmiany** - nowe role dodawane co tydzień/miesiąc
- **Rich metadata** - role potrzebują opisów, ograniczeń, atrybutów
- **Duża skala** - setki/tysiące osób pełniących te same role
- **Dynamiczne środowisko** - organizacje często się reorganizują

---

## Porównanie Końcowe

| Aspekt | String | Inheritance | PartyRoleType |
|--------|--------|-------------|---------------|
| **Prostota** | ✓ Bardzo prosta | ✗ Wiele klas | ~ Średnia |
| **Type-safety** | ✗ Brak | ✓ Compile-time | ~ Runtime |
| **Czas implementacji** | ✓ Godziny | ~ Dni | ~ Tydzień |
| **Runtime config** | ~ Możliwa | ✗ Niemożliwa | ✓ Pełna |
| **Dodawanie roli** | ✓ Natychmiastowe | ✗ Kod + deploy | ✓ Przez UI/API |
| **Multi-tenant** | ✗ Trudne | ✗ Niemożliwe | ✓ Native |
| **Metadata** | ✗ Brak miejsca | ~ W kodzie | ✓ W katalogu |
| **Specyficzne zachowania** | ✗ Niemożliwe | ✓ Metody w klasie | ~ Przez strategy |
| **Raportowanie** | ✗ Problematyczne | ~ Możliwe | ✓ Łatwe |
| **Ewolucja** | ✗ Chaotyczna | ✗ Sztywna | ✓ Kontrolowana |

### Macierz Decyzyjna

```
Wybierz STRING jeśli:
├─ Masz <= 5 ról
├─ MVP lub prototyp
├─ Role rzadko się zmieniają (< raz na kwartał)
└─ Mały zespół (wszyscy znają role)

Wybierz INHERITANCE jeśli:
├─ System regulowany (bankowość, medycyna)
├─ Krytyczne bezpieczeństwo
├─ Role mają specyficzne zachowania (metody)
├─ Role stabilne (zmiany < raz na rok)
└─ Type-safety jest wymaganiem

Wybierz PARTYROLETYPE jeśli:
├─ Multi-tenant SaaS
├─ Self-service dla klientów
├─ Częste zmiany (nowe role co tydzień/miesiąc)
├─ Role potrzebują metadata (opisy, constraints)
├─ Duża skala (setki/tysiące ról)
└─ Dynamiczne środowisko organizacyjne
```

---

## Następne Kroki

W naszym przypadku (archetypy jako wzorce architektoniczne) wybieramy **PartyRoleType**, ponieważ:

1. **Archetyp ma pokazywać enterprise patterns** - Type-Instance to fundamentalny wzorzec
2. **Analogia do ProductType** - spójność między archetypami
3. **Elastyczność** - użytkownicy archetypów będą mieli różne potrzeby
4. **Możliwość rozszerzeń** - łatwo dodać Responsibilities, Capabilities, Career Paths, etc.

Zakodujemy migrację z `PartyRole(String)` na `PartyRoleType + PartyRole`:
1. Stworzenie `PartyRoleType` jako katalog
2. Zmiana `PartyRole` aby referencjonował typ
3. Dodanie `PartyRoleConstraint` do walidacji
4. Adaptacja `PartyRoleDefiningPolicy`
5. Testy pokazujące wszystkie scenariusze
