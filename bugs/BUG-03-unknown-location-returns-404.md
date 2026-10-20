# BUG-03 – PUT cursus met onbekende locationId geeft 404 in plaats van 400

> **Bevestigd** met eigen Postman-run op 20/10/2026 (08:47): TC08.03 faalt met `Onbekende locatie geeft 400: status: expected 404 to deeply equal 400` (de assertion-tekst is omgedraaid, de SUT geeft dus 404 terwijl 400 verwacht is). Eerder ook gezien in een run van een collega (12/10/2026).

| Veld | Waarde |
|---|---|
| ID | BUG-03 |
| Ernst / prioriteit | Minor / Laag |
| User story | US-12 – Cursus wijzigen |
| Endpoint | `PUT /api/admin/courses/{id}` |
| Omgeving | Team 2 – `https://qacademyb.duckdns.org` |
| Gevonden door | TC08.03 (collectie in deze repo), run 20/10/2026 |
| Status | Open |

## Omschrijving
Een update met een `locationId` die niet bestaat, geeft `404 Not Found`. Volgens de specificatie is dat een validatiefout (`400`). Een 404 suggereert dat de **cursus** uit de URL niet bestaat. Een client kan de situaties "cursus bestaat niet" en "ongeldige locatie in de body" zo niet onderscheiden. Voor een onbekende `instructorId` geeft hetzelfde endpoint wel correct `400` (TC-23c).

## Verwacht resultaat (volgens US-12)
> 400 – startDateTime/endDateTime form an invalid window, minAge outside 0-120, unknown locationId/instructorIds.

`400 Bad Request` met een melding over de locatie.

## Werkelijk resultaat
`404 Not Found` – `"Location not found: 00000000-0000-0000-0000-000000000000"`

## Stappen om te reproduceren
1. Token van admin.test.
2. Neem een bestaande cursus (bv. een DRAFT-cursus).
3. Stuur:

```http
PUT /api/admin/courses/{courseId}
Authorization: Bearer <admin token>
Content-Type: application/json

{
  "title": "QA Draft Course",
  "price": 99,
  "capacityPolicy": "WAITLIST",
  "locationId": "00000000-0000-0000-0000-000000000000"
}
```

## Bewijs
```json
{"timestamp":"2026-10-12T14:37:00.623436435Z","status":404,"error":"Not Found",
 "message":"Location not found: 00000000-0000-0000-0000-000000000000"}
```

Newman: `TC-23b Update with unknown locationId -> 400 › Status code is 400 – expected response to have status code 400 but got 404`
