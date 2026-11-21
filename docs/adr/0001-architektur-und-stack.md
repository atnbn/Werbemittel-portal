# ADR 0001: Architektur und Stack

Status: Akzeptiert
Datum: 2026-11-21

## Kontext

Gebaut wird eine Miniatur eines Local-Marketing-Portals: Eine Markenzentrale stellt
Werbemittel-Vorlagen bereit, Vertriebspartner personalisieren und bestellen sie, und
ein Werbekostenzuschuss wird von ihrem Guthaben abgebucht. Die fachlichen Anforderungen
stehen in `docs/spec.md`.

Rahmenbedingungen: Für die Umsetzung stehen etwa 2,5 Tage zur Verfügung. Der Code wird
von einem Coding-Agenten (Claude Code) geschrieben; ich spezifiziere, steuere und prüfe
das Ergebnis über Tests und Code-Review. Ziel ist eine vollständig lauffähige Demo mit
automatisierten Tests und CI, nicht ein produktionsreifes System.

## Entscheidungen

### 1. Backend: Java 25 + Spring Boot 4

Das Backend wird mit Java 25 (LTS) und Spring Boot 4 umgesetzt, gebaut mit dem Maven Wrapper.
Verwendete Module: Spring Web (REST), Spring Data JPA (Persistenz), Spring Security
(Authentifizierung und Rollen), Bean Validation (Eingabeprüfung).

Begründung:

- Spring Boot liefert die benötigten Querschnittsthemen fertig mit: Dependency Injection,
  Validierung, Security, Transaktionen und Fehlerantworten nach RFC 9457 (Problem Details).
  Dadurch entsteht wenig eigener Infrastruktur-Code, und der Fokus liegt auf der Fachlogik.
- Java mit Spring Boot ist ein verbreiteter Standard für Geschäftsanwendungen mit klarer
  Struktur, die sich gut für objektorientiertes Design und automatisierte Tests eignet.
- Mein Schwerpunkt liegt bisher auf TypeScript. Der Java-Code wird von einem Coding-Agenten
  geschrieben; ich steuere über Spec, ADR und CLAUDE.md und prüfe das Ergebnis über Tests
  und Code-Review. Diese Arbeitsweise ist Teil dessen, was das Projekt zeigen soll.

### 2. Frontend: React + TypeScript + Vite

Das Frontend wird als Single-Page-Application mit React und TypeScript umgesetzt,
gebaut mit Vite.

Begründung:

- **Erfahrung:** React und TypeScript sind mein Schwerpunkt. Dadurch kann ich den vom
  Agenten erzeugten Frontend-Code am zuverlässigsten prüfen.
- **Passend zum Kern-Feature:** Der Vorlagen-Editor mit Live-Vorschau ist ein
  State-getriebenes UI: Die Feldwerte liegen im Komponentenzustand, die Vorschau wird
  daraus abgeleitet und bei jeder Eingabe neu gerendert.
- **TypeScript als API-Vertrag:** Die Typen für Requests und Responses werden aus
  Spec, Abschnitt 9, abgeleitet und an einer Stelle gepflegt (`src/api/types.ts`).
  Abweichungen zwischen Frontend und API fallen beim Kompilieren auf statt im Betrieb.
- **Vite:** schneller Dev-Server, minimale Konfiguration, offizielle Vorlage für React + TypeScript.

Umsetzung:

- Alle API-Aufrufe laufen über ein zentrales Modul (`src/api/client.ts`), das nur die
  Endpunkte aus der Spec kennt. Komponenten rufen `fetch` nicht direkt auf.
- Im Entwicklungsmodus leitet der Vite-Proxy `/api` an das Backend weiter; dadurch ist
  keine CORS-Konfiguration nötig.
- Die gesperrten Felder sind im Editor nur zur Bedienbarkeit deaktiviert; die
  verbindliche Prüfung erfolgt im Backend (Entscheidung 5).
- Bewusst keine zusätzliche State-Management- oder UI-Bibliothek: React-State und
  einfache Hooks reichen für den Umfang.

### 3. Datenbank: H2 (In-Memory)

Als Datenbank wird H2 im In-Memory-Modus verwendet. Das Schema erzeugt JPA aus den
Entitäten; Seed-Daten (Spec, Abschnitt 10) werden beim Start geladen.

Begründung:

- Kein Setup: Die Anwendung und alle Integrationstests laufen ohne externe Datenbank
  und ohne Docker, lokal wie in der CI.
- Jeder Start beginnt mit einem bekannten Datenstand, was Demo und Tests reproduzierbar macht.

### 4. Authentifizierung: HTTP Basic statt JWT

Die API wird mit HTTP Basic über Spring Security abgesichert. Benutzer liegen in der
Datenbank, Passwörter als BCrypt-Hash. Es gibt zwei Rollen: PARTNER und ZENTRALE.

Die Autorisierung erfolgt auf zwei Ebenen:

- **Rollenebene** (Wer darf welchen Endpunkt aufrufen?): zentral in der Security-Konfiguration,
  z. B. `POST /api/orders` nur für PARTNER, `PATCH /api/orders/{id}/status` nur für ZENTRALE.
- **Datenebene** (Wem gehört welche Bestellung?): im Service. Ein Partner erhält nur eigene
  Bestellungen; fremde werden wie nicht existierende behandelt (404, AK-12).

Der Partner einer Bestellung wird immer aus dem angemeldeten Benutzer ermittelt,
nie aus dem Request.

Begründung: HTTP Basic ist ohne zusätzlichen Code verfügbar und reicht, um Rollen und
Zugriffsregeln vollständig zu zeigen. Die Autorisierungslogik ist unabhängig vom
Anmeldeverfahren und bleibt bei einem späteren Wechsel unverändert.

### 5. Markenkonformität über Feldtypen statt Freigabe-Workflow

Werbemittel werden nicht einzeln von der Zentrale freigegeben. Stattdessen legt die
Zentrale pro Vorlagenfeld fest, ob es fest (FIXED), aus Stammdaten befüllt
(PARTNER_DATA) oder vom Partner änderbar (EDITABLE) ist.

Begründung: Ein manueller Freigabeschritt skaliert bei vielen Partnern schlecht und
verzögert die Bestellung. Wenn nur freigegebene Felder änderbar sind, ist jedes
Werbemittel per Konstruktion markenkonform. Das entspricht dem Ansatz etablierter
Local-Marketing-Plattformen, bei denen die Zentrale geprüfte Vorlagen bereitstellt.

Die Regel wird **im Backend** durchgesetzt (AK-4, AK-5). Die Sperre im Editor ist nur
Komfort; ein manipulierter Request darf keine gesperrten Felder ändern.

### 6. Geldbeträge als ganzzahlige Cent

Alle Beträge werden als `long` in Cent gespeichert und berechnet. Der Zuschuss wird
nach der Regel in Spec, Abschnitt 5, auf ganze Cent abgerundet.

Begründung:

- Gleitkommazahlen (`double`) können Dezimalbeträge nicht exakt darstellen
  (0.1 + 0.2 ≠ 0.3); bei Geld führt das zu Rundungsfehlern.
- Ganzzahlige Cent sind exakt, einfach zu testen und eindeutig zu serialisieren.
- `BigDecimal` wäre ebenfalls korrekt, ist für Beträge mit fester Genauigkeit von zwei
  Nachkommastellen aber unnötig aufwendig.

### 7. Schichtenarchitektur: Controller → Service → Domain / Repository

Das Backend ist nach Fachmodulen gegliedert (`template`, `order`, `partner`, `security`),
innerhalb jedes Moduls in Schichten. Daneben gibt es `common` (fachliche Exceptions,
zentrale Fehlerabbildung), das von keinem Fachmodul abhängt, und `seed`, das die Seed-Daten
lädt, alle Fachmodule kennen darf und von keinem Modul verwendet wird. Fachmodule dürfen von
`security` abhängen, `security` von keinem Fachmodul.

| Schicht    | Verantwortung                                                                                                                 | Enthält keine                         |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| Controller | HTTP: Request/Response-DTOs, Formatvalidierung, Statuscodes                                                                   | Geschäftsregeln                       |
| Service    | Ablauf eines Anwendungsfalls, Transaktion, Zugriffsprüfung auf Datenebene                                                     | HTTP-Details                          |
| Domain     | Geschäftsregeln: Statusübergänge (`OrderStatus`), Preisberechnung (`PriceCalculator`), Feldprüfung (`TemplateValueValidator`) | Spring- oder Datenbank-Abhängigkeiten |
| Repository | Laden und Speichern über Spring Data JPA                                                                                      | Geschäftslogik                        |

Entitäten werden nicht direkt über die API ausgegeben; Controller arbeiten mit DTOs,
deren Form in Spec, Abschnitt 9, festgelegt ist. Fehler werden als fachliche Exceptions
geworfen und zentral in einem `@RestControllerAdvice` auf Problem Details abgebildet.

Begründung:

- **Single Responsibility:** Jede Klasse hat genau einen Grund, sich zu ändern.
- **Testbarkeit:** Die Domain-Regeln sind reine Java-Klassen und per Unit-Test ohne
  Spring-Kontext prüfbar; Integrationstests prüfen das Zusammenspiel über HTTP.
- **Open/Closed:** Ein neuer Status oder ein neuer Feldtyp erweitert die Domain, ohne
  Controller oder Security anzupassen.
- **Entkopplung der API:** DTOs verhindern, dass interne Felder (z. B. `passwordHash`)
  versehentlich nach außen gelangen und dass Datenbankänderungen die API brechen.
- Die Struktur gibt dem Coding-Agenten ein festes Muster vor, das er für jedes neue
  Feature wiederverwendet.

### 8. Sprache: Dokumentation Deutsch, Code Englisch

Spec, ADRs, README und CLAUDE.md sind auf Deutsch. Code, Bezeichner, Testnamen,
Commit-Nachrichten und Branch-Namen sind auf Englisch.

Begründung:

- Die Fachsprache des Kontexts ist Deutsch (Zentrale, Vertriebspartner,
  Werbekostenzuschuss). Fachliche Dokumente in dieser Sprache vermeiden
  Übersetzungsfehler und sind für Fachseite und Entwicklung gleichermaßen lesbar.
- Englisch im Code ist Industriestandard und passt zu Java, Spring und den
  verwendeten Bibliotheken.
- Das Glossar in Spec, Abschnitt 3, ordnet jedem Fachbegriff genau einen Namen
  im Code zu (Ubiquitous Language). Neue Begriffe werden zuerst dort eingetragen.

Ausnahme: Die Werte von `OrderStatus` (BESTELLT, IN_PRODUKTION, VERSENDET) bleiben
deutsch, da sie Fachbegriffe sind und unverändert in der API und im UI erscheinen.

## Konsequenzen

**Positiv**

- Geschäftsregeln sind an genau einer Stelle implementiert und isoliert getestet.
- Anwendung und Tests starten ohne externe Abhängigkeiten.
- Rollen- und Datenzugriff sind klar getrennt und einzeln testbar.

**Negativ / bewusst in Kauf genommen**

- Daten gehen bei jedem Neustart verloren (H2 In-Memory).
- H2 verhält sich in Details anders als eine produktive Datenbank.
- HTTP Basic überträgt Zugangsdaten bei jedem Request und ist nur mit HTTPS vertretbar.
- Gleichzeitige Bestellungen desselben Partners könnten das Guthaben doppelt verbrauchen.
  Der Bestellvorgang läuft in einer Transaktion (`@Transactional`); gegen parallele
  Zugriffe ist das allein nicht ausreichend.

**Für einen Produktivbetrieb zu ändern**

- PostgreSQL mit versionierten Migrationen (Flyway) statt H2 und automatisch erzeugtem Schema.
- HTTPS und tokenbasierte Anmeldung oder Single Sign-on statt HTTP Basic.
- Optimistic Locking (`@Version`) auf `Partner`, damit parallele Abbuchungen erkannt werden.

## Verworfene Alternativen

| Alternative                           | Warum verworfen                                                                                                                  |
| ------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Node/TypeScript-Backend               | Ein durchgehender Stack wäre schneller umsetzbar, zeigt aber keine Erfahrung mit Java/Spring Boot und den dort üblichen Mustern. |
| JWT                                   | Erfordert Token-Ausgabe, -Prüfung und Ablaufbehandlung; für die gezeigten Rollenregeln ohne Mehrwert.                            |
| PostgreSQL                            | Realistischer, erfordert aber eine laufende Datenbank für Entwicklung, Tests und CI.                                             |
| `double` für Beträge                  | Rundungsfehler, siehe Entscheidung 6.                                                                                            |
| Logik im Controller                   | Nicht isoliert testbar, verletzt Single Responsibility, wird bei jedem Feature unübersichtlicher.                                |
| Angular                               | Im Java-Umfeld verbreitet, aber für den Umfang schwergewichtig                                                                   |
| Server-Side-Rendering (z. B. Next.js) | Kein Bedarf an SEO oder Server-Rendering für ein internes Portal; zusätzliche Komplexität.                                       |
| Freigabe-Workflow durch die Zentrale  | Skaliert bei vielen Partnern schlecht und verzögert Bestellungen; siehe Entscheidung 5.                                          |
