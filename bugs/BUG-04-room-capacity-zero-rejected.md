# BUG-04 – Locatie aanmaken met roomCapacity 0 wordt geweigerd, terwijl de specificatie 0 toelaat

> **Bevestigd** met eigen Postman-run op 20/10/2026 (08:47) en met GitHub Actions run #1: TC05.02 faalt met `Status is 201: expected response to have status code 201 but got 400`.

| Veld | Waarde |
|---|---|
| ID | BUG-04 |
| Ernst / prioriteit | Minor / Midden |
| User story | US-23 – Locatie aanmaken |
| Endpoint | `POST /api/admin/locations` |
| Rol / token | ADMIN (admin.test) |
| Omgeving | Team 2 – `https://qacademyb.duckdns.org` |
| Gevonden door | TC05.02 (grenswaarde roomCapacity 0), run 20/10/2026 |
| Status | Open |

## Omschrijving
`roomCapacity` is optioneel en heeft in de Swagger (`LocationRequest`) een ondergrens van 0. De SUT weigert echter de waarde 0 en vraagt "greater than 0". De implementatie wijkt dus af van zijn eigen specificatie: de grenswaarde 0 is volgens de spec geldig maar wordt als fout behandeld.

## Verwacht resultaat
`201 Created` met `roomCapacity: 0` (grenswaarde, minimum 0 volgens de Swagger).

## Werkelijk resultaat
`400 Bad Request` met de melding `roomCapacity must be greater than 0`.

## Stappen om te reproduceren
1. Token van admin.test.
2. Stuur:

```http
POST /api/admin/locations
Authorization: Bearer <admin token>
Content-Type: application/json

{
  "name": "QA Zaal test",
  "address": "Teststraat 1",
  "city": "Hasselt",
  "country": "Belgie",
  "roomCapacity": 0
}
```

## Bewijs
```json
{"timestamp":"2026-10-20T06:50:22.171502195Z","status":400,"error":"Bad Request",
 "message":"roomCapacity must be greater than 0"}
```

Postman-run: `TC05.02 Locatie aanmaken (roomCapacity 0)` – `Status is 201 | expected response to have status code 201 but got 400`.

## Gevolgschade in de testsuite
`loc2` wordt niet aangemaakt, waardoor TC05.03, TC05.04, TC05.07, TC05.09 en TC05.10 meefalen (URL `/locations/null`). Dat zijn gevolgfouten van deze ene bug, geen aparte bugs.

## Niet getest
Of `PUT /api/admin/locations/{id}` met `roomCapacity: 0` ook geweigerd wordt, is niet apart getest.
