# PERF-01 – Checkout-endpoint heeft trage en sterk wisselende responstijden

> **Bevestigd** met eigen metingen op 20/10/2026 (twee Postman-runs en een GitHub Actions-run). Eerder ook gezien in een run van een collega (12/10/2026).

| Veld | Waarde |
|---|---|
| ID | PERF-01 |
| Type | Niet-functioneel (performance) |
| Ernst / prioriteit | Major / Midden |
| User story | US-07 – Checkout |
| Endpoint | `POST /api/payments/{subscriptionId}/checkout` (method `EXTERNAL`) |
| Omgeving | Team 2 – `https://qacademyb.duckdns.org` |
| Gevonden door | TC17.01 (collectie in deze repo), run 20/10/2026 |
| Status | Open |

## Omschrijving
De checkout met `EXTERNAL` (een mock gateway die altijd slaagt) is opvallend traag en de responstijd schommelt sterk tussen calls. Alle andere endpoints antwoorden stabiel rond 30–130 ms. Een mock gateway zonder echte externe betaling zou niet trager mogen zijn dan de andere schrijfoperaties.

## Verwacht resultaat
- Elke checkout < 500 ms (`perf_checkout_max_ms` in de environment).
- Spreiding (max − min) over 5 calls < 300 ms (`perf_spread_ms`).

## Werkelijk resultaat
| Meting | Responstijden checkout (ms) | Spreiding |
|---|---|---|
| Postman-run 20/10/2026 08:40 (5×) | 1206, 962, 1626, 1396, 1255 | 664 ms |
| Postman-run 20/10/2026 08:47 (5×) | checkout 1 = 917 en checkout 3 = 1368 boven de grens, de andere drie < 500 | 1290 ms |
| GitHub Actions run #1 (5×) | 925, 1598, 1231, 648, 2003 | 1355 ms |
| Collega-run Newman 12/10/2026 (5×) | 1838, 526, 1929, 871, 616 | 1403 ms |
| Collega-run curl 12/10/2026 (8×) | 351, 1517, 1929, 173, 726, 1421, 1495, 249 | – |
| Vergelijking: `DELETE /api/subscriptions/{id}` (8×) | 96 – 104 | – |
| Vergelijking: `GET /api/subscriptions/me`, `/api/exam-results/me`, `/api/admin/courses`, `/subscribers`, `/attendance` | 92 – 133 | – |

De checkout duurt tussen ≈ 170 ms en ≈ 2003 ms. Dat is tot 20× trager dan de andere endpoints. Het aantal checkouts boven 500 ms verschilt per run (3 tot 6 van de 6 asserties in TC17.01 falen), maar de spreiding van meer dan 300 ms trad in elke meting op.

## Stappen om te reproduceren
1. Token van student.fresh.
2. Herhaal 5×:
   1. `POST /api/subscriptions {"courseId": "<betalende, gepubliceerde cursus>"}` → `subId`
   2. `POST /api/payments/<subId>/checkout {"method": "EXTERNAL"}` → noteer de responstijd
   3. `DELETE /api/subscriptions/<subId>` (zodat je opnieuw kan inschrijven)

In de collectie automatiseert request `TC17.01 Checkout 5x meten (drempel en spreiding)` dit. De grenzen staan in de environment als `perf_checkout_max_ms` en `perf_spread_ms`.

## Impact
Studenten wachten tot ~2 s op de bevestiging van hun betaling. Bij gelijktijdig gebruik kan dat tot time-outs of dubbele klikken, en dus dubbele betaalpogingen, leiden.

## Opmerking over reproduceerbaarheid
Het aantal falende asserties in TC17.01 verschilt per run (19 tot 22 falende tests in totaal) omdat de responstijd wisselt. Dat is zelf de bevinding: de checkout is niet voorspelbaar snel.
