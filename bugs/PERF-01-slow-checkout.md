# PERF-01 – Checkout-endpoint heeft trage en sterk wisselende responstijden

> **Bevestigd** met eigen Postman-run op 20/10/2026 (08:47, TC17.01). Eigen metingen: run 1 (08:40) checkout 1206, 962, 1626, 1396 en 1255 ms (spreiding 664 ms); run 2 (08:47) checkout 1 = 917 ms en checkout 3 = 1368 ms boven de grens, spreiding 1290 ms. De checkout is dus structureel traag en wisselvallig, maar niet bij elke call boven 500 ms. Eerder ook gezien in een run van een collega (12/10/2026).

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
De checkout met `EXTERNAL` (een mock gateway die altijd slaagt) is opvallend traag, en de responstijd schommelt sterk tussen calls. Alle andere endpoints antwoorden stabiel rond 100 ms. Een mock gateway zonder echte externe betaling zou niet trager mogen zijn dan de andere schrijfoperaties.

## Verwacht resultaat
- Elke checkout < 500 ms (`perf_checkout_max_ms` in de environment).
- Spreiding (max − min) over 5 calls < 300 ms (`perf_spread_ms`).

## Werkelijk resultaat
| Meting | Responstijden (ms) |
|---|---|
| `POST /api/payments/{id}/checkout` – Newman-run (5×) | 1838, 526, 1929, 871, 616 → spreiding 1403 ms |
| `POST /api/payments/{id}/checkout` – curl tijdens verkenning (8×) | 351, 1517, 1929, 173, 726, 1421, 1495, 249 |
| Vergelijking: `DELETE /api/subscriptions/{id}` (8×) | 96 – 104 |
| Vergelijking: `GET /api/subscriptions/me`, `/api/exam-results/me`, `/api/admin/courses`, `/subscribers`, `/attendance` | 92 – 133 |

De checkout duurt tussen ≈ 170 ms en ≈ 1930 ms, gemiddeld ongeveer 1 s. Dat is 10× trager dan de andere endpoints, met een spreiding van bijna 2 s.

## Stappen om te reproduceren
1. Token van student.fresh.
2. Herhaal 5×:
   1. `POST /api/subscriptions {"courseId": "<betalende, gepubliceerde cursus>"}` → `subId`
   2. `POST /api/payments/<subId>/checkout {"method": "EXTERNAL"}` → noteer de responstijd
   3. `DELETE /api/subscriptions/<subId>` (zodat je opnieuw kan inschrijven)

In de collectie automatiseert `09 - Performance › PERF-01` dit (aantal iteraties via `perfIterations`).

## Impact
Studenten wachten tot ~2 s op de bevestiging van hun betaling. Bij gelijktijdig gebruik kan dat tot time-outs of dubbele klikken (en dus dubbele betaalpogingen) leiden.
