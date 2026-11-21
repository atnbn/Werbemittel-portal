---
name: implement-from-spec
description: Setzt ein Feature anhand von Akzeptanzkriterien aus docs/spec.md test-first um. Aufruf mit AK-IDs, z. B. /implement-from-spec AK-3 AK-4
argument-hint: AK-IDs, z. B. AK-3 AK-4
disable-model-invocation: true
---

# Feature aus der Spec umsetzen

Umzusetzende Akzeptanzkriterien: $ARGUMENTS

Halte dich an CLAUDE.md. Arbeite die Phasen strikt nacheinander ab und
**halte nach Phase 2 an**, bis ich den Plan freigebe.

## Phase 1 – Verstehen

1. Lies die genannten AKs in docs/spec.md und gib sie wörtlich wieder.
2. Liste alle betroffenen Endpunkte mit der Zeile aus Spec, Abschnitt 9.
   Ein Endpunkt, der dort nicht steht, wird nicht gebaut.
3. Liste die betroffenen Entitäten und Felder aus Spec, Abschnitt 8.
4. Nenne Unklarheiten oder Lücken in der Spec. Gibt es welche: anhalten und fragen.

## Phase 2 – Planen

1. Sieh dir die bestehende Code-Struktur an und nenne das Modul, dessen Muster du übernimmst.
2. Liste jede Datei, die du anlegst oder änderst, mit einem Satz zum Zweck.
3. Ordne jedem AK die geplanten Tests zu (Testname mit AK-ID, Unit- oder Integrationstest).
4. Nenne Entscheidungen, die nicht in Spec oder ADR stehen.

**→ Anhalten. Warte auf meine Freigabe.**

5. Lege den freigegebenen Plan inklusive meiner Änderungen als `docs/plans/<Feature>.md`
   ab (z. B. `docs/plans/F2.md`), bevor Phase 3 beginnt.

## Phase 3 – Tests zuerst

1. Schreibe die geplanten Tests.
2. Führe sie aus und zeige, dass die neuen Tests aus dem richtigen Grund rot sind
   (fehlende Funktion, nicht Kompilier- oder Konfigurationsfehler in anderen Teilen).
3. Halte das Ergebnis des roten Testlaufs (Anzahl Failures und Grund je Testklasse) in
   `docs/plans/<Feature>.md` unter „Phase 3: Rot-Nachweis“ fest.
4. Prüfe bei jedem roten Test, dass er aus dem erwarteten Grund rot ist (fehlende Umsetzung), nicht wegen eines Fehlers im Test selbst.

## Phase 4 – Umsetzen

1. Implementiere nur, was die Tests grün macht.
2. Führe die komplette Test-Suite aus (`.\mvnw.cmd verify` bzw. `npm test`).

## Phase 5 – Selbstprüfung

Erstelle eine Tabelle:

| AK | Test(s) | Ergebnis | Anmerkung |

Prüfe zusätzlich und bestätige jeden Punkt:

- [ ] Keine Endpunkte, Felder oder Parameter außerhalb der Spec
- [ ] Keine Geschäftslogik im Controller
- [ ] Keine Entität direkt in einer API-Antwort
- [ ] Kein Test abgeschwächt, gelöscht oder deaktiviert
- [ ] Keine neuen Dependencies

## Phase 6 – Abschluss

Schlage eine Commit-Nachricht nach Conventional Commits vor. Committe nicht selbst.
