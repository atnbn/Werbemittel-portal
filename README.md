# Werbemittel-Portal

[![CI](https://github.com/atnbn/Werbemittel-portal/actions/workflows/ci.yml/badge.svg)](https://github.com/atnbn/Werbemittel-portal/actions/workflows/ci.yml)

Eine Miniatur eines Marketingportals für Vertriebspartner: Die Zentrale stellt Vorlagen mit gesperrten und editierbaren Feldern bereit, Partner personalisieren sie im Editor mit Live-Vorschau und bestellen. Der Werbekostenzuschuss wird vom Guthaben des Partners abgebucht, die Zentrale setzt den Status bis zum Versand.

Das Projekt zeigt vor allem **wie** es entstanden ist: spec-first, mit Claude Code als ausführendem Agenten und mir als Planer und Reviewer.

## Workflow

| Schritt                | Was passiert                                                                                                                                                                                 | Artefakt                                                                                     |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| Konzept                | Fachlichkeit, Glossar, Akzeptanzkriterien als Given/When/Then, Endpunkte, Abgrenzung                                                                                                         | [`docs/spec.md`](docs/spec.md)                                                               |
| Architektur            | Stack, Security, Modulgrenzen, verworfene Alternativen                                                                                                                                       | [`docs/adr/0001-architektur-und-stack.md`](docs/adr/0001-architektur-und-stack.md)           |
| Regeln für den Agenten | Harte Regeln (nur Spec-Endpunkte, keine neuen Dependencies, Tests nie abschwächen), Modulabhängigkeiten, Lessons Learned                                                                     | [`CLAUDE.md`](CLAUDE.md)                                                                     |
| Umsetzung              | Skill `/implement-from-spec AK-…` in sechs Phasen: Verstehen → Plan → Tests rot → Umsetzen → Selbstprüfung → Commit-Vorschlag. Der Agent hält nach Phase 1 und 2 an und wartet auf Freigabe. | [`.claude/skills/implement-from-spec/SKILL.md`](.claude/skills/implement-from-spec/SKILL.md) |
| Plan je Feature        | Dateien, Testtabelle, Entscheidungen, Rot-Nachweis                                                                                                                                           | [`docs/plans/`](docs/plans/) (F1–F3 Backend, F4–F6 Frontend)                                 |
| Review/QA              | Plan-Review, Diff-Review, CI, manueller Durchlauf; jede Abweichung des Agenten wird protokolliert                                                                                            | [`docs/agent-log.md`](docs/agent-log.md)                                                     |

Jeder Test beginnt mit der ID seines Akzeptanzkriteriums (`ak8_…`, `ui5_…`), so lässt sich jedes AK bis zum Test zurückverfolgen. Ich committe selbst, der Agent schlägt nur vor.

## Agent-Log: Auszug

Echte Fehler und Funde aus der Entwicklung, das vollständige Log hat [22 Einträge](docs/agent-log.md).

| Was passiert ist                                                                                                                                                | Wie bemerkt                                        | Folge                                                                                     |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| Der Plan für das Bestellen hatte keinen Schutz gegen parallele Requests. Das Guthaben wäre nur einmal belastet worden (Lost Update).                            | Plan-Review: Geldfluss auf Nebenläufigkeit geprüft | Pessimistische Sperre, Entscheidung im ADR                                                |
| Der Agent änderte nach der Umsetzung einen Test, ohne zu fragen. Inhaltlich korrekt, aber unkontrolliert.                                                       | Selbstprüfung im Bericht, Spec nachgezählt         | Neue Regel: Teständerung nach Phase 3 nur mit Freigabe. Sie hat später zweimal gegriffen. |
| Ein Test war im Rot-Nachweis rot, aber aus dem falschen Grund: Er hätte nie grün werden können (geschütztes Leerzeichen).                                       | Agent hielt vor der Teständerung an                | Neue Skill-Regel: Rot muss den erwarteten Grund zeigen                                    |
| Die Preisvorschau im Frontend war nur mit den Spec-Beispielen getestet. Eine Umsetzung mit Gleitkomma-Quote hätte sie bestanden und trotzdem falsch abgerundet. | Review: Rechnung mit dem Backend verglichen        | Ganzzahlige Rechnung vorgegeben, zusätzlicher Testfall                                    |
| Derselbe Widerspruch zwischen Spec und CLAUDE.md tauchte zweimal auf, weil die erste Korrektur nur den Einzelfall behob.                                        | Agent meldete es beide Male selbst                 | Regel allgemein formuliert statt Aufzählung                                               |
| Erster CI-Lauf rot, weil `mvnw` im Commit nicht ausführbar war.                                                                                                 | GitHub Actions                                     | Neue Regel: Ein Schritt gilt erst mit grüner CI als erledigt                              |

## Starten

Voraussetzungen: Java 25, Node 22.

```bash
# Backend auf http://localhost:8080
cd backend && ./mvnw spring-boot:run

# Frontend auf http://localhost:5173 (leitet /api an das Backend weiter)
cd frontend && npm install && npm run dev
```

Demo-Zugänge (Passwort jeweils `demo`):

| Benutzer   | Rolle    | Zum Ausprobieren                         |
| ---------- | -------- | ---------------------------------------- |
| `partner1` | PARTNER  | Vorlage personalisieren und bestellen    |
| `partner2` | PARTNER  | Geringes Guthaben, zeigt Hinweis und 422 |
| `zentrale` | ZENTRALE | Alle Bestellungen, Status weiterschalten |

## Tests und CI

- Backend: Unit- und Integrationstests mit JUnit und MockMvc ([9 Tests), `./mvnw verify`
- Frontend: Vitest und Testing Library (42 Tests), `npm test`
- GitHub Actions führt bei jedem Push beide Suiten und den Frontend-Build aus.
- Vor jedem Feature-Abschluss manuell durchgespielt: Login, Bestellen, 422 bei zu wenig Guthaben, Umlaute, Statuswechsel.

## Stack

Java 25, Spring Boot 4.2, Spring Security (HTTP Basic), H2 · React 19, TypeScript, Vite · Begründungen und verworfene Alternativen im [ADR](docs/adr/0001-architektur-und-stack.md).
