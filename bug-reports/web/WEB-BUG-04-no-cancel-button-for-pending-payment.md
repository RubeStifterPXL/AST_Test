# WEB-BUG-04 – Geen annuleerknop voor een onbetaalde inschrijving (PENDING_PAYMENT)

| Veld | Waarde |
|---|---|
| Bug-ID | WEB-BUG-04 |
| User story | US-11 – Als student kan ik een inschrijving annuleren (API US-06: "Any status except CANCELLED can be cancelled") |
| Pagina | `/my-subscriptions` |
| Rol | STUDENT (student.fresh) |
| Omgeving | Team 2 – https://qacademyb.duckdns.org, Chromium (CI) |
| Ernst / prioriteit | Middel / Middel |
| Testgeval | `CancelSubscriptionTest.cancelPendingPayment` |
| Reproduceerbaar | Ja: faalt in elke run, ook bij de retry |
| Status | Open |

## Precondities
Eigen betaalde cursus (20 EUR), gepubliceerd. student.fresh schrijft in via de API → `PENDING_PAYMENT`.

## Stappen om te reproduceren
1. Log in als `student.fresh` en open **My subscriptions**.
2. Bekijk het item van de cursus (status `PENDING_PAYMENT`).

## Verwacht resultaat
Naast *Pay with wallet* en *Pay with card (mock)* een knop **Cancel** (`cancel-subscription-button-<id>`), want de backend laat annuleren toe voor elke status behalve `CANCELLED`. Een student die niet wil betalen, moet zijn inschrijving kunnen intrekken.

## Actueel resultaat
Enkel de twee betaalknoppen. De knop Cancel verschijnt pas bij `CONFIRMED` of `WAITLISTED`:

```
waiting for locator("[data-testid^='subscription-item-']").filter(hasText "QA ... Cancel pending ...")
    .locator("[data-testid^='cancel-subscription-button-']")
Timeout 15000ms exceeded.
```

In de broncode van de frontend (`my-subscriptions`): de knop wordt enkel gerenderd als
`status === 'CONFIRMED' || status === 'WAITLISTED'`.

## Bewijs
- CI: job `chromium / student-certificates`.
- Screenshot: artifact `web-traces-chromium-student-certificates-<run>` → `CancelSubscriptionTest-cancelPendingPayment-*.png`.
- Via de API werkt annuleren wel: `DELETE /api/subscriptions/<id>` → 204 (getest in PE1, TC11).

## Impact
De student blijft met een openstaande inschrijving zitten die hij via de UI niet kan intrekken.
