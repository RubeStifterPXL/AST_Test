# BUG-02 – Wachtlijst promoveert de jongste in plaats van de oudste WAITLISTED inschrijving

> **Bevestigd** met eigen Postman-run op 20/10/2026 (08:47): TC13.05 faalt met `student2 (oudste) is doorgeschoven naar CONFIRMED: expected 'WAITLISTED' to deeply equal 'CONFIRMED'` en `student3 blijft WAITLISTED: expected 'CONFIRMED' to deeply equal 'WAITLISTED'`. Eerder ook gezien in een run van een collega (12/10/2026).

| Veld | Waarde |
|---|---|
| ID | BUG-02 |
| Ernst / prioriteit | Major / Hoog |
| User story | US-06 – Inschrijving annuleren (wachtlijstpromotie) |
| Endpoint | `DELETE /api/subscriptions/{id}` |
| Omgeving | Team 2 – `https://qacademyb.duckdns.org` |
| Gevonden door | TC13.05 (collectie in deze repo); eerder gevonden met een collega-run |
| Status | Open |

## Omschrijving
Annuleert iemand een `CONFIRMED` plaats op een `WAITLIST`-cursus, dan promoveert het systeem de **meest recente** wachtende inschrijving. Het hoort de **oudste** te zijn. Wie het langst wacht, wordt dus overgeslagen.

## Verwacht resultaat (volgens US-06)
> Cancelling a CONFIRMED seat on a course with a waitlist promotes the single oldest WAITLISTED subscriber (by subscribedAt) – that subscriber moves to PENDING_PAYMENT if the course is paid.

student.second (eerst op de wachtlijst) → `PENDING_PAYMENT`; student.third → blijft `WAITLISTED`.

## Werkelijk resultaat
student.second blijft `WAITLISTED`; student.third (later ingeschreven) wordt `PENDING_PAYMENT`.

## Precondities
Cursus met `capacityPolicy = WAITLIST`, `maxSubscribers = 1`, prijs 50 EUR, status `PUBLISHED`.

## Stappen om te reproduceren
1. student.fresh: `POST /api/subscriptions {"courseId": "<id>"}` → `PENDING_PAYMENT`
2. student.fresh: `POST /api/payments/<subId>/checkout {"method": "EXTERNAL"}` → `CONFIRMED` (cursus is nu vol)
3. student.second: `POST /api/subscriptions {"courseId": "<id>"}` → `WAITLISTED` (subscribedAt 14:37:17.013)
4. student.third: `POST /api/subscriptions {"courseId": "<id>"}` → `WAITLISTED` (subscribedAt 14:37:17.134)
5. student.fresh: `DELETE /api/subscriptions/<subId>` → `204`
6. admin: `GET /api/admin/courses/<id>/subscribers`

## Bewijs
```json
[
  {"email": "student.fresh@qacademy.local",  "status": "CANCELLED",       "subscribedAt": "2026-10-12T14:37:16.601984Z"},
  {"email": "student.second@qacademy.local", "status": "WAITLISTED",      "subscribedAt": "2026-10-12T14:37:17.013506Z"},
  {"email": "student.third@qacademy.local",  "status": "PENDING_PAYMENT", "subscribedAt": "2026-10-12T14:37:17.134508Z"}
]
```

Newman: `SC-02.10 Roster: oldest waitlisted (student2) promoted › student2 is PENDING_PAYMENT – expected 'WAITLISTED' to deeply equal 'PENDING_PAYMENT'`

Ook handmatig gereproduceerd tijdens de verkenning, met hetzelfde resultaat.
