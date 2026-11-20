# CLAUDE.md – Mini-Marketingportal

## Quellen der Wahrheit

- Fachliche Anforderungen: @docs/spec.md
- Architekturentscheidungen: @docs/adr/0001-architektur-und-stack.md

Spec und ADR haben Vorrang vor eigenen Annahmen. Wenn Code und Spec sich widersprechen,
ist der Code falsch. Wenn die Spec lückenhaft oder widersprüchlich ist: **anhalten und
nachfragen**, nicht raten.

## Arbeitsablauf pro Feature

1. **Verstehen:** Nenne die betroffenen Akzeptanzkriterien (AK-IDs) und Endpunkte aus der Spec.
2. **Planen:** Beschreibe kurz, welche Klassen du anlegst oder änderst. Warte auf meine Freigabe.
3. **Tests zuerst:** Schreibe die Tests für die AKs. Führe sie aus und zeige, dass sie rot sind.
4. **Umsetzen:** Implementiere nur so viel, dass die Tests grün werden.
5. **Prüfen:** Führe alle Tests aus. Gehe jedes betroffene AK durch und bestätige es einzeln.
6. **Abschließen:** Schlage eine Commit-Nachricht vor. Ich prüfe den Diff und committe selbst.

## Harte Regeln

- **Nur Endpunkte aus Spec, Abschnitt 9.** Keine zusätzlichen Endpunkte, Parameter oder
  Felder. Fehlt etwas, schlage eine Spec-Änderung vor, statt es einzubauen.
- **Nur Feld- und Klassennamen aus dem Glossar** (Spec, Abschnitt 3) und dem Datenmodell
  (Abschnitt 8). Neue Begriffe zuerst im Glossar vorschlagen.
- **Keine Abweichung vom ADR** (z. B. kein JWT, keine andere Datenbank, keine Logik im
  Controller) ohne meine ausdrückliche Freigabe.
- **Keine neuen Dependencies** ohne Freigabe.
- **Tests niemals abschwächen, löschen oder deaktivieren** (`@Disabled`, auskommentierte
  Asserts, gelockerte Erwartungen), um sie grün zu bekommen. Ist ein Test falsch, sag es
  und begründe es.
- **Der Partner einer Bestellung kommt immer aus dem angemeldeten Benutzer**, nie aus dem Request.
- **Entitäten nie direkt über die API ausgeben**, immer DTOs in der Form aus Spec, Abschnitt 9.

## Prinzipien

- **SOLID**, insbesondere Single Responsibility: eine Klasse, ein Grund für Änderungen.
- **Bestehende Muster wiederverwenden:** Bevor du eine neue Klasse anlegst, sieh dir an,
  wie ein vergleichbares Modul aufgebaut ist, und folge derselben Struktur und Benennung.
- **Kein Code auf Vorrat:** Nur bauen, was ein AK verlangt.

## Backend (`backend/`)

- Java 25, Spring Boot 4, Maven Wrapper. Basis-Package: `com.miniportal`.
- Struktur pro Fachmodul (`template`, `order`, `partner`, `security`, `common`):
  - `XxxController`: nur HTTP, Mapping auf DTOs, `@Valid`
  - `XxxService`: Ablauf, `@Transactional`, Zugriffsprüfung auf Datenebene
  - Domain-Klassen (`OrderStatus`, `PriceCalculator`, `TemplateValueValidator`):
    reine Java-Klassen ohne Spring-Annotationen
  - `XxxRepository`: Spring Data JPA
  - `dto/`: Records für Requests und Responses
- Fehler: fachliche Exceptions in `common`, Abbildung auf Problem Details nur im
  zentralen `@RestControllerAdvice`.
- Beträge immer `long` in Cent.

## Frontend (`frontend/`)

- React + TypeScript + Vite, `strict` in tsconfig, kein `any`.
- API-Typen ausschließlich in `src/api/types.ts`, exakt nach Spec, Abschnitt 9.
- API-Aufrufe ausschließlich über `src/api/client.ts`. Komponenten rufen kein `fetch` auf.
- Keine zusätzlichen UI- oder State-Bibliotheken.

## Tests

- Jedes AK hat mindestens einen Test. Der Testname beginnt mit der AK-ID, z. B.
  `ak7_orderIsRejectedWhenBalanceIsInsufficient`.
- Domain-Regeln: Unit-Tests mit JUnit 5 und AssertJ, ohne Spring-Kontext.
- Endpunkte, Rollen, Fehlercodes: Integrationstests mit `@SpringBootTest` und MockMvc.
- Bei Fehlerantworten `status` und `detail` prüfen, nicht `title`.

## Befehle

- Backend testen: `cd backend; .\mvnw.cmd verify` (Linux/CI: `./mvnw verify`)
- Backend starten: `cd backend; .\mvnw.cmd spring-boot:run`
- Frontend: `cd frontend; npm run dev` / `npm run build` / `npm test`

## Definition of Done

- Alle Tests grün (`mvnw verify`, `npm run build`, `npm test`).
- Jedes betroffene AK ist durch einen Test abgedeckt und wurde einzeln bestätigt.
- Keine TODOs, kein auskommentierter Code, keine neuen Endpunkte außerhalb der Spec.

## Commits

Conventional Commits auf Englisch: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `ci:`, `chore:`.
Ein Feature pro Commit.

## Eigene Erfahrungen (Lessons Learned)

- **Erfundene API-Endpunkte:** In einem früheren Projekt hat der Agent einen Endpunkt
  verwendet, den es nicht gab, weil ihm der Kontext fehlte. Der Fehler fiel erst über
  404-Antworten auf. Deshalb: Bevor du einen Endpunkt aufrufst oder implementierst,
  prüfe, dass er in Spec, Abschnitt 9, steht, und nenne die Zeile. Steht er dort nicht,
  halte an und frag nach.
