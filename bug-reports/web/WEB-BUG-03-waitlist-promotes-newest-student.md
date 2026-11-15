# WEB-BUG-03 – Vrijgekomen plaats gaat niet naar de oudste wachtende student

| Veld | Waarde |
|---|---|
| Bug-ID | WEB-BUG-03 (zelfde oorzaak als API [BUG-02](../api/BUG-02-waitlist-promotes-newest-subscriber.md)) |
| User story | US-11 – Als student kan ik een inschrijving annuleren; een vrijgekomen plaats gaat naar de wachtlijst |
| Pagina | `/my-subscriptions` |
| Rol | STUDENT (student.fresh, student.second, student.third) |
| Omgeving | Team 2 – https://qacademyb.duckdns.org, Chromium (CI) |
| Ernst / prioriteit | Hoog / Hoog |
| Testgeval | `CancelSubscriptionTest.freedSeatGoesToOldestWaitlistedStudent` |
| Reproduceerbaar | Ja: faalt in elke run, ook bij de retry |
| Status | Open |

## Precondities
Eigen gratis cursus, `WAITLIST`, `maxSubscribers = 1`, gepubliceerd. Via de API:
1. student.fresh schrijft in → `CONFIRMED` (cursus vol);
2. student.second schrijft in → `WAITLISTED` (oudste wachtende);
3. 1 seconde later schrijft student.third in → `WAITLISTED` (nieuwste wachtende).

## Stappen om te reproduceren
1. Log in als `student.fresh`, open **My subscriptions** en klik bij de cursus op **Cancel**.
2. Status wordt `CANCELLED` (correct).
3. Log in als `student.second` en open **My subscriptions**.

## Verwacht resultaat
student.second (oudste op de wachtlijst) staat op `CONFIRMED` (gratis cursus); student.third blijft `WAITLISTED`.

## Actueel resultaat
student.second blijft `WAITLISTED`:

```
Locator expected to have text: CONFIRMED
Received:  WAITLISTED
  - waiting for locator("[data-testid^='subscription-item-']").filter(hasText "QA ... Promote ...")
      .locator("[data-testid^='subscription-status-']")
```

Met maar één wachtende (`promotedStudentOnPaidCourseMustPay`) wordt die wel correct doorgeschoven (naar `PENDING_PAYMENT`): het probleem zit dus in de volgorde, niet in het doorschuiven zelf.

## Bewijs
- CI: job `chromium / student-certificates`, run 43736940822 en volgende.
- Trace: artifact `web-traces-chromium-student-certificates-<run>` → `CancelSubscriptionTest-freedSeatGoesToOldestWaitlistedStudent-*.zip`
  (drie contexten: student.fresh, student.second, student.third).

## Impact
Wie het langst wacht, wordt overgeslagen; oneerlijk en in strijd met de specificatie.
