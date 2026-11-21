# Agent-Log

Abweichungen des Agenten und wie ich sie bemerkt und korrigiert habe.

| #   | Feature / AK | Was der Agent gemacht hat                                                              | Wie ich es bemerkt habe                                    | Korrektur                                                           | Neue Regel?                              |
| --- | ------------ | -------------------------------------------------------------------------------------- | ---------------------------------------------------------- | ------------------------------------------------------------------- | ---------------------------------------- |
| 2   | F1 / Plan    | Wollte den Seed-Loader in `common` legen, obwohl er alle Fachmodule kennt              | Review des Plans in Phase 2: Abhängigkeitsrichtung geprüft | Eigenes Package `seed`                                              | Ja: Modulliste in CLAUDE.md ergänzt      |
| 3   | F1 / Plan    | Kein Muster für Test-Isolation, obwohl ab F2 Tests Daten verändern                     | Review: Vorausschauend auf F2 geprüft                      | `@Transactional` auf Integrationstests                              | Ja, in CLAUDE.md unter Tests             |
| 4   | F1 / Plan    | Erkannte den Zyklus security ↔ partner selbst, schlug aber vor, ihn beizubehalten      | Review der offenen Entscheidungen nach Phase 3             | Fachmodule → security, User hält nur `partnerId`, eigener Principal | Ja: Abhängigkeitsregel in CLAUDE.md      |
| 5   | F1 / Phase 3 | Plan fehlte nach Sessionwechsel im Kontext, Agent rekonstruierte ihn aus altem Verlauf | Agent meldete es selbst                                    | Plan als Datei `docs/plans/F1.md`                                   | Ja: Skill Phase 2 legt Plan als Datei ab |
