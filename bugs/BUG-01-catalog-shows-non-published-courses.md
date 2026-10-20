# BUG-01 – Publieke catalogus toont cursussen met status DRAFT en CANCELLED

> **Bevestigd** met eigen Postman-run op 20/10/2026 (08:47): TC01.01, TC01.02, TC08.10 en TC08.15 falen. Eerder ook gezien in een run van een collega (12/10/2026).

| Veld | Waarde |
|---|---|
| ID | BUG-01 |
| Ernst / prioriteit | Major / Hoog |
| User story | US-01 – Publieke catalogus |
| Endpoint | `GET /api/public/courses` |
| Omgeving | Team 2 – `https://qacademyb.duckdns.org` |
| Gevonden door | TC01.01, TC01.02, TC08.10, TC08.15 (collectie in deze repo); eerder gevonden met een collega-run |
| Status | Open |

## Omschrijving
De lijst van de publieke catalogus filtert niet op status. Cursussen met status `DRAFT` (nooit gepubliceerd of via unpublish teruggezet) en `CANCELLED` worden aan anonieme bezoekers getoond. Dat gebeurt met en zonder filters. Bezoekers zien zo cursussen die (nog) niet bestaan of geannuleerd zijn, en ook interne werktitels zoals "Security Testing Essentials (draft)".

## Verwacht resultaat (volgens US-01)
> DRAFT/CANCELLED/COMPLETED courses never appear here, regardless of filters.

Elke cursus in `content` heeft `status = "PUBLISHED"`.

## Werkelijk resultaat
De catalogus bevat ook de seed-cursus `security-testing-essentials` (`DRAFT`) en elke DRAFT- of CANCELLED-cursus die de testen aanmaken.

## Stappen om te reproduceren
1. Geen token nodig.
2. `GET https://qacademyb.duckdns.org/api/public/courses?size=100`
3. Bekijk het veld `status` van elke cursus in `content`.

Variant (scenario SC-04):
1. Admin maakt een cursus aan, plant ze en publiceert ze (`POST /api/admin/courses/{id}/publish`).
2. Admin zet ze terug met `POST /api/admin/courses/{id}/unpublish` (status `DRAFT`) of annuleert ze met `POST /api/admin/courses/{id}/cancel` (status `CANCELLED`).
3. `GET /api/public/courses?q=<titel>` → de cursus wordt nog steeds gevonden.

## Bewijs
```http
GET /api/public/courses?q=QA%20Scenario%20States%201791815813204
200 OK

{"content":[{"id":"0f65fe52-bd15-44da-9586-70d1ffee0a85","title":"QA Scenario States 1791815813204",
  "slug":"sc-04-1791815813204", ..., "status":"CANCELLED", ...}],
 "page":0,"size":20,"totalElements":1,"totalPages":1}
```

```http
GET /api/public/courses
200 OK

... {"id":"20000000-0000-0000-0000-000000000004","title":"Security Testing Essentials (draft)",
     "slug":"security-testing-essentials","status":"DRAFT", ...}
```

Newman: `TC-02 Catalog only contains PUBLISHED courses › Every listed course has status PUBLISHED – non-published courses in catalog: expected [ …(3) ] to be empty`

## Opmerking
Het detail-endpoint `GET /api/public/courses/{id}` mag volgens US-02 elke status teruggeven. Dat is dus geen bug; TC-17 test dit en slaagt.
