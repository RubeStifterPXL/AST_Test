# WEB-BUG-02 – Instructor ziet e-mailadressen van studenten in de inschrijvingen

| Veld | Waarde |
|---|---|
| Bug-ID | WEB-BUG-02 |
| User story | US-14 – Als instructor bekijk ik de inschrijvingen van mijn cursus; e-mailadressen van studenten zijn niet zichtbaar |
| Pagina | `/courses/{id}/subscribers` (data-testid `course-subscribers-page`) |
| Rol | INSTRUCTOR (instructor.test, toegewezen aan de cursus) |
| Omgeving | Team 2 – https://qacademyb.duckdns.org, Chromium (CI) |
| Ernst / prioriteit | Hoog / Hoog (privacy, GDPR) |
| Testgeval | `InstructorEnrollmentsTest.instructorDoesNotSeeEmailAddresses` |
| Reproduceerbaar | Ja: faalt in elke run, ook bij de retry |
| Status | Open |

## Precondities
Eigen gepubliceerde, gratis cursus met instructor.test als docent; student.fresh is ingeschreven (CONFIRMED).

## Stappen om te reproduceren
1. Log in als `instructor.test`.
2. Ga naar **My courses** en klik bij de cursus op **Subscribers**
   (of open rechtstreeks `/courses/<courseId>/subscribers`).
3. Bekijk de tabel `subscriber-table`.

## Verwacht resultaat
Naam en status van elke student, **geen** kolom *Email* en nergens een e-mailadres (US-14).

## Actueel resultaat
De tabel heeft de kolommen `Name | Email | Status | Subscribed at` en toont het e-mailadres van elke student
(bv. `student.fresh@...`).

```
[kolomtitels voor een instructor]
Expecting no elements of:
  ["Name", "Email", "Status", "Subscribed at"]
to match given predicate but this element did:
  "Email"
```

## Bewijs
- CI: job `chromium / instructor-e2e`, run 43736940822 en volgende.
- Trace + screenshot: artifact `web-traces-chromium-instructor-e2e-<run>` → `InstructorEnrollmentsTest-instructorDoesNotSeeEmailAddresses-*.zip`.
- Ter vergelijking slaagt `adminSeesEmailAddresses`: voor de admin is het e-mailadres wél bedoeld (US-20).

## Impact
Persoonsgegevens van studenten worden getoond aan docenten die ze niet nodig hebben (dataminimalisatie). De frontend gebruikt voor instructors hetzelfde admin-endpoint (`/api/admin/courses/{id}/subscribers`, met e-mail) en hetzelfde template als voor de admin.

## Vermoedelijke oorzaak
`CourseSubscribersComponent` toont altijd de kolom Email; er is geen aparte, afgeslankte respons of rolcontrole in het template.
