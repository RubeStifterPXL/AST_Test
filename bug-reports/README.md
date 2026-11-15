# Bugrapporten QAcademy (team 2)

Eén bestand per bug. Sjablonen: [`api/BUG-000-template.md`](api/BUG-000-template.md), [`web/WEB-BUG-000-template.md`](web/WEB-BUG-000-template.md).

## Web (PE2, Playwright)

| ID | Titel | Ernst | User story | Test | Gerelateerd |
|---|---|---|---|---|---|
| [WEB-BUG-01](web/WEB-BUG-01-catalog-shows-draft-and-cancelled-courses.md) | Publieke catalogus toont DRAFT- en CANCELLED-cursussen | Hoog | US-01 | `CatalogBrowseTest` (2 tests) | API BUG-01 |
| [WEB-BUG-02](web/WEB-BUG-02-instructor-sees-student-emails.md) | Instructor ziet e-mailadressen van studenten | Hoog | US-14 | `InstructorEnrollmentsTest` | - |
| [WEB-BUG-03](web/WEB-BUG-03-waitlist-promotes-newest-student.md) | Vrijgekomen plaats gaat niet naar de oudste wachtende | Hoog | US-11 | `CancelSubscriptionTest` | API BUG-02 |
| [WEB-BUG-04](web/WEB-BUG-04-no-cancel-button-for-pending-payment.md) | Geen annuleerknop voor PENDING_PAYMENT | Middel | US-11 | `CancelSubscriptionTest` | - |

## API (PE1, Postman/Newman)

| ID | Titel |
|---|---|
| [BUG-01](api/BUG-01-catalog-shows-non-published-courses.md) | Publieke catalogus toont niet-gepubliceerde cursussen |
| [BUG-02](api/BUG-02-waitlist-promotes-newest-subscriber.md) | Wachtlijst promoveert de nieuwste inschrijving |
| [BUG-03](api/BUG-03-unknown-location-returns-404.md) | Onbekende locatie geeft 404 in plaats van 400 |
| [BUG-04](api/BUG-04-room-capacity-zero-rejected.md) | roomCapacity 0 wordt geweigerd |
| [PERF-01](api/PERF-01-slow-checkout.md) | Checkout traag en wisselend |
