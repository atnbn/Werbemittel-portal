# Spec: Mini-Marketingportal

## 1. Ziel und Kontext

Markenzentralen arbeiten mit vielen lokalen Vertriebspartnern. Die Zentrale stellt
markenkonforme Werbemittel-Vorlagen bereit; Partner personalisieren sie mit ihren
eigenen Daten und bestellen sie. Hersteller bezuschussen die Werbung über einen
Werbekostenzuschuss. Markenkonformität entsteht dadurch, dass Partner nur die
Felder ändern können, die die Zentrale freigegeben hat.

## 2. Rollen

| Rolle    | Darf                                                                                           |
| -------- | ---------------------------------------------------------------------------------------------- |
| PARTNER  | Vorlagen ansehen, personalisieren, bestellen; eigene Bestellungen und eigenes Guthaben ansehen |
| ZENTRALE | alle Vorlagen und alle Bestellungen ansehen; Bestellstatus ändern                              |

## 3. Begriffe (Glossar)

| Fachbegriff         | Im Code       | Bedeutung                                                                        |
| ------------------- | ------------- | -------------------------------------------------------------------------------- |
| Vorlage             | Template      | Werbemittel mit festem Layout und Feldern                                        |
| Feld                | TemplateField | Ein Textbaustein der Vorlage                                                     |
| Stammdaten          | Partner       | Firmenname, Adresse, Telefon des Partners                                        |
| Werbekostenzuschuss | subsidy       | Anteil der Kosten, der vom Guthaben abgebucht wird                               |
| Eigenanteil         | ownShare      | Rest, den der Partner selbst zahlt                                               |
| Bestellstatus       | OrderStatus   | BESTELLT, IN_PRODUKTION, VERSENDET (Enum-Werte bleiben deutsch, da Fachbegriffe) |
| Rolle               | Role          | PARTNER, ZENTRALE                                                                |
| Vorlagentyp         | TemplateType  | FLYER, POSTER, NEWSPAPER_AD                                                      |
| Feldtyp             | FieldType     | FIXED, PARTNER_DATA, EDITABLE                                                    |

## 4. Feldtypen einer Vorlage

| Typ          | Wer setzt den Wert                                                   | Darf der Partner ändern |
| ------------ | -------------------------------------------------------------------- | ----------------------- |
| FIXED        | Zentrale (Standardwert)                                              | nein                    |
| PARTNER_DATA | automatisch aus Stammdaten (`partnerAttribute`, z. B. `companyName`) | nein                    |
| EDITABLE     | Partner, sonst Standardwert                                          | ja, bis `maxLength`     |

Jede Vorlage hat ein PARTNER_DATA-Feld mit key `companyName` und partnerAttribute `companyName`.

## 5. Berechnung einer Bestellung

- Gesamtpreis = Menge × Stückpreis
- Zuschuss = Gesamtpreis × Zuschussquote, abgerundet auf ganze Cent
- Eigenanteil = Gesamtpreis − Zuschuss
- Der Zuschuss wird bei der Bestellung vom Guthaben abgebucht.
- Alle Beträge in Cent (ganzzahlig).

**Beispiel 1:** Menge 500, Stückpreis 12 Cent, Quote 50 %
→ Gesamtpreis 6.000 Cent (60 €), Zuschuss 3.000 Cent (30 €), Eigenanteil 3.000 Cent (30 €).
Das Guthaben sinkt um 3.000 Cent.

**Beispiel 2 (Abrundung):** Menge 333, Stückpreis 12 Cent, Quote 33 %
→ Gesamtpreis 3.996 Cent, Zuschuss 1.318 Cent (exakt 1.318,68, abgerundet), Eigenanteil 2.678 Cent.

## 6. Akzeptanzkriterien

Tests referenzieren die ID im Testnamen (z. B. `ak3_...`).

**Katalog**

- AK-1: Given eingeloggt (beliebige Rolle), When ich `GET /api/templates` aufrufe, Then erhalte ich alle Vorlagen mit ihren Feldern.
- AK-2: Given nicht eingeloggt, When ich einen beliebigen `/api`-Endpunkt aufrufe, Then 401.
- AK-19: Given eingeloggt (beliebige Rolle), When ich `GET /api/templates/{id}` mit einer bestehenden `id` aufrufe,
  Then 200 und die Vorlage mit ihren Feldern; When die `id` unbekannt ist, Then 404.

**Benutzer**

- AK-20: Given eingeloggt (beliebige Rolle), When ich `GET /api/me` aufrufe, Then 200 mit `username` und `role` des angemeldeten Benutzers.
- AK-21: Given eingeloggt als PARTNER, When ich `GET /api/partners/me` aufrufe, Then 200 mit den eigenen Stammdaten und dem Guthaben (`balanceCents`).
  Given eingeloggt als ZENTRALE, When ich `GET /api/partners/me` aufrufe, Then 403.

**Bestellen**

- AK-3: Given eingeloggt als PARTNER mit ausreichendem Guthaben, When ich eine Vorlage mit gültigen Werten und Menge ≥ 1 bestelle,
  Then 201, Status BESTELLT, Preis/Zuschuss/Eigenanteil nach Abschnitt 5, Guthaben um den Zuschuss reduziert.
- AK-4: Given PARTNER, When ich einen Wert für ein FIXED- oder PARTNER_DATA-Feld oder einen Key, den die Vorlage nicht hat, mitsende,
  Then 400; nichts wird gespeichert und das Guthaben bleibt unverändert.
- AK-5: Given PARTNER, When der Wert eines EDITABLE-Felds länger als maxLength ist, Then 400; nichts wird gespeichert und das Guthaben bleibt unverändert.
- AK-6: Given PARTNER, When Menge < 1, Menge oder templateId fehlt oder ein Wert in values null ist, Then 400; nichts wird gespeichert.
- AK-7: Given PARTNER mit Guthaben kleiner als der Zuschuss, When ich bestelle, Then 422 und das Guthaben bleibt unverändert.
- AK-8: Given eine bestehende Bestellung, When sich danach der Firmenname des Partners oder der Standardwert eines Vorlagenfelds ändert,
  Then liefert `GET /api/orders/{id}` weiterhin die Werte vom Bestellzeitpunkt. (Test ändert die Daten direkt über das Repository.)
- AK-9: Given ZENTRALE, When ich eine Bestellung aufgebe, Then 403; nichts wird gespeichert und kein Guthaben verändert.
- AK-16: Given PARTNER, When ich mit einer unbekannten `templateId` bestelle, Then 404.

**Bestellungen ansehen**

- AK-10: Given PARTNER, When `GET /api/orders`, Then erhalte ich nur eigene Bestellungen.
- AK-11: Given ZENTRALE, When `GET /api/orders`, Then erhalte ich alle Bestellungen.
- AK-12: Given PARTNER, When ich eine fremde Bestellung per ID abrufe, Then 404, damit deren Existenz nicht offengelegt wird.

**Status**

- AK-13: Given ZENTRALE und Bestellung BESTELLT, When ich den Status auf IN_PRODUKTION setze, Then 200.
- AK-14: Given ZENTRALE, When ein Übergang nicht in Abschnitt 7 steht (z. B. BESTELLT → VERSENDET), Then 409.
- AK-15: Given PARTNER, When ich den Status ändern will, Then 403.
- AK-17: Given ZENTRALE, When ich den Status einer unbekannten Bestellung ändern will, Then 404.
- AK-18: Given ZENTRALE, When ich einen unbekannten Statuswert sende (z. B. `"FOO"`) oder `status` fehlt, Then 400.

## 7. Statusmodell

| Von           | Nach          | Rolle    |
| ------------- | ------------- | -------- |
| BESTELLT      | IN_PRODUKTION | ZENTRALE |
| IN_PRODUKTION | VERSENDET     | ZENTRALE |

VERSENDET ist Endzustand. Alle anderen Übergänge → 409.

## 8. Datenmodell

**User**: id, username, passwordHash, role (PARTNER | ZENTRALE), partner (nur bei PARTNER)
**Partner**: id, companyName, street, zip, city, phone, balanceCents
**Template**: id, name, type (FLYER | POSTER | NEWSPAPER_AD), unitPriceCents, subsidyPercent (0–100), fields
**TemplateField**: id, key, label, fieldType (FIXED | PARTNER_DATA | EDITABLE), defaultValue, partnerAttribute, maxLength, position
**Order**: id, partner, template, quantity, status, totalCents, subsidyCents, ownShareCents, createdAt, updatedAt
**OrderFieldValue**: id, order, key, value

## 9. REST-Schnittstellen

Basis `/api`, HTTP Basic, JSON, Fehler als Problem Details (RFC 9457).
Auch 401 und 403 werden als Problem Details geliefert. Ein `WWW-Authenticate`-Header wird nicht gesendet,
damit der Browser keinen eigenen Login-Dialog öffnet.
**Es gibt ausschließlich diese Endpunkte. Neue Endpunkte nur nach Änderung dieser Spec.**

| Methode | Pfad                    | Rolle                           | Erfolg                 | Fehler                  |
| ------- | ----------------------- | ------------------------------- | ---------------------- | ----------------------- |
| GET     | /api/me                 | beide                           | 200 `{username, role}` | 401                     |
| GET     | /api/partners/me        | PARTNER                         | 200 Partner            | 401, 403                |
| GET     | /api/templates          | beide                           | 200 Liste von Template | 401                     |
| GET     | /api/templates/{id}     | beide                           | 200 Template           | 401, 404                |
| POST    | /api/orders             | PARTNER                         | 201 Order              | 400, 401, 403, 404, 422 |
| GET     | /api/orders             | PARTNER: eigene, ZENTRALE: alle | 200 Liste von Order    | 401                     |
| GET     | /api/orders/{id}        | Eigentümer oder ZENTRALE        | 200 Order              | 401, 404                |
| PATCH   | /api/orders/{id}/status | ZENTRALE                        | 200 Order              | 400, 401, 403, 404, 409 |

### GET /api/me – Response

```json
{ "username": "partner1", "role": "PARTNER" }
```

### GET /api/partners/me – Response

```json
{
  "id": 1,
  "companyName": "Autohaus Müller",
  "street": "Hauptstr. 1",
  "zip": "96106",
  "city": "Ebern",
  "phone": "09531 12345",
  "balanceCents": 10000
}
```

### Template – Response

```json
{
  "id": 1,
  "name": "Flyer Frühling",
  "type": "FLYER",
  "unitPriceCents": 12,
  "subsidyPercent": 50,
  "fields": [
    {
      "key": "claim",
      "label": "Marken-Claim",
      "fieldType": "FIXED",
      "defaultValue": "Gemeinsam mehr erreichen.",
      "partnerAttribute": null,
      "maxLength": null,
      "position": 1
    },
    {
      "key": "companyName",
      "label": "Firmenname",
      "fieldType": "PARTNER_DATA",
      "defaultValue": null,
      "partnerAttribute": "companyName",
      "maxLength": null,
      "position": 2
    },
    {
      "key": "headline",
      "label": "Überschrift",
      "fieldType": "EDITABLE",
      "defaultValue": "Jetzt vorbeikommen!",
      "partnerAttribute": null,
      "maxLength": 40,
      "position": 3
    }
  ]
}
```

Listen von Vorlagen sind nach `id` sortiert, `fields` nach `position`.

### POST /api/orders – Request

```json
{
  "templateId": 1,
  "quantity": 500,
  "values": { "headline": "Frühlingsaktion!" }
}
```

`values` enthält nur EDITABLE-Felder; fehlende EDITABLE-Felder bekommen den Standardwert.
`values` ist optional; fehlt es, gilt `{}`. Leere Strings sind erlaubt.

### Order – Response

```json
{
  "id": 7,
  "templateId": 1,
  "templateName": "Flyer Frühling",
  "partnerId": 1,
  "quantity": 500,
  "status": "BESTELLT",
  "totalCents": 6000,
  "subsidyCents": 3000,
  "ownShareCents": 3000,
  "values": {
    "claim": "Gemeinsam mehr erreichen.",
    "companyName": "Autohaus Müller",
    "headline": "Frühlingsaktion!"
  },
  "createdAt": "2026-11-21T10:00:00Z",
  "updatedAt": "2026-11-21T10:00:00Z"
}
```

`values` ist der Snapshot aller Feldwerte zum Bestellzeitpunkt (AK-8).
Listen von Bestellungen sind nach `id` sortiert.

### PATCH /api/orders/{id}/status – Request

```json
{ "status": "IN_PRODUKTION" }
```

Ein Statuswechsel setzt `updatedAt` auf den Zeitpunkt der Änderung.

### Fehler – Response (Beispiel)

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Guthaben reicht nicht für den Zuschuss von 3000 Cent."
}
```

Tests prüfen `status` und `detail`, nicht `title`.

## 10. Seed-Daten

- Nutzer: `zentrale`, `partner1` (Guthaben 100,00 €), `partner2` (Guthaben 5,00 €)
- Passwort für alle Demo-Nutzer: `demo` (nur für die lokale Demo, gespeichert als BCrypt-Hash)
- 3 Vorlagen: Flyer, Plakat, Zeitungsanzeige, je mit mind. einem Feld jedes Typs
- Stammdaten:
  - partner1: Autohaus Müller, Hauptstr. 1, 96106 Ebern, 09531 12345, Guthaben 10.000 Cent
  - partner2: Fahrrad Schmidt, Bahnhofstr. 5, 96047 Bamberg, 0951 98765, Guthaben 500 Cent
- Vorlagen (Felder in der Reihenfolge von `position`, beginnend bei 1):
  - 1 „Flyer Frühling“, FLYER, 12 Cent, 50 %:
    - claim „Marken-Claim“ (FIXED, „Gemeinsam mehr erreichen.“)
    - companyName „Firmenname“ (PARTNER_DATA → companyName)
    - headline „Überschrift“ (EDITABLE, „Jetzt vorbeikommen!“, max 40)
  - 2 „Plakat Sommer“, POSTER, 250 Cent, 40 %:
    - claim „Marken-Claim“ (FIXED, „Gemeinsam mehr erreichen.“)
    - companyName „Firmenname“ (PARTNER_DATA → companyName)
    - phone „Telefon“ (PARTNER_DATA → phone)
    - headline „Überschrift“ (EDITABLE, „Sommerangebote“, max 30)
  - 3 „Zeitungsanzeige Herbst“, NEWSPAPER_AD, 4.900 Cent, 30 %:
    - claim „Marken-Claim“ (FIXED, „Gemeinsam mehr erreichen.“)
    - companyName „Firmenname“ (PARTNER_DATA → companyName)
    - city „Ort“ (PARTNER_DATA → city)
    - offer „Angebotstext“ (EDITABLE, „Jetzt Termin vereinbaren.“, max 60)

## 11. Frontend

React + TypeScript + Vite (ADR, Entscheidung 2). Nutzt ausschließlich die Endpunkte aus Abschnitt 9.
Zugangsdaten liegen nur im Speicher (kein localStorage); nach einem Reload ist eine neue Anmeldung nötig.
Beträge werden aus Cent als Euro im Format de-DE angezeigt (z. B. 6000 → „60,00 €“).
Schlägt ein API-Aufruf fehl, zeigt die Seite `detail` aus der Fehlerantwort an. Ausnahme: 401 beim Login zeigt „Anmeldung fehlgeschlagen.“

### Akzeptanzkriterien

- UI-1 Login: Given die Login-Seite, When ich gültige Zugangsdaten eingebe, Then prüft die App sie über `GET /api/me` und zeigt für PARTNER den Katalog, für ZENTRALE die Bestellübersicht. Bei 401 erscheint „Anmeldung fehlgeschlagen.“
- UI-2 Katalog: Given PARTNER, When der Katalog lädt, Then sehe ich alle Vorlagen mit Name, Typ, Stückpreis und Zuschussquote sowie mein Guthaben.
- UI-3 Editor: Given PARTNER öffnet eine Vorlage, Then werden FIXED-Felder und PARTNER_DATA-Felder (aus `GET /api/partners/me`) schreibgeschützt angezeigt und EDITABLE-Felder als Eingabe mit maxLength und Zeichenzähler.
- UI-4 Live-Vorschau: Given der Editor, When ich ein EDITABLE-Feld ändere, Then aktualisiert sich die Vorschau des Werbemittels sofort mit allen Feldwerten in der Reihenfolge von `position`.
- UI-5 Preisvorschau: Given der Editor, When ich die Menge ändere, Then sehe ich Gesamtpreis, Zuschuss und Eigenanteil nach Abschnitt 5. Ist das Guthaben kleiner als der Zuschuss, erscheint ein Hinweis. Verbindlich ist die Berechnung des Servers.
- UI-6 Bestellen: Given gültige Eingaben, When ich bestelle, Then wird `POST /api/orders` gesendet; bei 201 erscheint die Bestellung unter „Meine Bestellungen“, bei einem Fehler wird `detail` der Antwort angezeigt.
- UI-7 Meine Bestellungen: Given PARTNER, Then sehe ich meine Bestellungen mit Vorlage, Menge, Status, Zuschuss und Eigenanteil.
- UI-8 Bestellübersicht Zentrale: Given ZENTRALE, Then sehe ich alle Bestellungen mit Firmenname (aus dem Snapshot), Vorlage, Status und Beträgen. Pro Bestellung gibt es nur einen Button für den nächsten erlaubten Status (Abschnitt 7); bei VERSENDET keinen.
- UI-9 Abmelden: Given angemeldet, When ich auf „Abmelden“ klicke, Then sind die Zugangsdaten gelöscht und die Login-Seite erscheint.

### Tests

- Reine Funktionen mit Vitest: Euro-Formatierung, Preisvorschau (Beispiele 1 und 2 aus Abschnitt 5), Zusammensetzen der Vorschau, nächster erlaubter Status.
- Komponenten mit Vitest + Testing Library: jedes UI-AK hat mindestens einen Komponententest.

## 12. Nicht im Umfang

Vorlagen anlegen per UI, Bild-/Logo-Upload, PDF-Export, Stornierung, Dienstleister-Anbindung, SSO.
Im Frontend: Routing-Bibliothek, UI-Framework, Responsive-Optimierung, Persistenz der Anmeldung.
