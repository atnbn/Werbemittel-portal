# Agent-Log

Abweichungen des Agenten und wie ich sie bemerkt und korrigiert habe.

| #   | Feature / AK | Was der Agent gemacht hat | Wie ich es bemerkt habe | Korrektur | Neue Regel? |
| --- | ------------ | ------------------------- | ----------------------- | --------- | ----------- |

| 1 | F1 / Phase 1 | Fand selbst eine Spec-Lücke: Endpunkte ohne AK. Hielt an und fragte nach, statt etwas anzunehmen | Agent meldete es selbst (Skill-Regel „bei Lücken anhalten“) | AK-19 bis AK-21 in die Spec aufgenommen | Nein, bestehende Regel hat gegriffen |

| 2 | F1 / Plan | Wollte den Seed-Loader in `common` legen, obwohl er alle Fachmodule kennt | Review des Plans in Phase 2: Abhängigkeitsrichtung geprüft | Eigenes Package `seed` | Ja: Modulliste in CLAUDE.md ergänzt |
| 3 | F1 / Plan | Kein Muster für Test-Isolation, obwohl ab F2 Tests Daten verändern | Review: Vorausschauend auf F2 geprüft | `@Transactional` auf Integrationstests | Ja, in CLAUDE.md unter Tests |
| 4 | F1 / Plan | Erkannte den Zyklus security ↔ partner selbst, schlug aber vor, ihn beizubehalten | Review der offenen Entscheidungen nach Phase 3 | Fachmodule → security, User hält nur `partnerId`, eigener Principal | Ja: Abhängigkeitsregel in CLAUDE.md |
| 5 | F1 / Phase 3 | Plan fehlte nach Sessionwechsel im Kontext, Agent rekonstruierte ihn aus altem Verlauf | Agent meldete es selbst | Plan als Datei `docs/plans/F1.md` | Ja: Skill Phase 2 legt Plan als Datei ab |
| 6 | F1 / Phase 4 | Schlug nach Sessionwechsel vor, Code zu stashen, um das Rot nachzuweisen. Der Stash ohne `-u` hätte den untracked Code gar nicht erfasst | Review des Vorschlags, Rot-Nachweis lag aus Phase 3 schon vor | Abgelehnt, Testergebnis aus Phase 3 als Nachweis | Ja: Phase-3-Ergebnis wird in `docs/plans/<Feature>.md` festgehalten |
