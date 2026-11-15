# WEB-BUG-01 – Publieke catalogus toont DRAFT- en CANCELLED-cursussen

| Veld | Waarde |
|---|---|
| Bug-ID | WEB-BUG-01 (zelfde oorzaak als API [BUG-01](../api/BUG-01-catalog-shows-non-published-courses.md)) |
| User story | US-01 – Als bezoeker bekijk ik de gepubliceerde cursussen |
| Pagina | Catalogus `/` (data-testid `course-list-page`) |
| Rol | Anonieme bezoeker |
| Omgeving | Team 2 – https://qacademyb.duckdns.org, Chromium (CI) |
| Ernst / prioriteit | Hoog / Hoog |
| Testgevallen | `CatalogBrowseTest.draftCourseIsNotListed`, `CatalogBrowseTest.cancelledCourseIsNotListed` |
| Reproduceerbaar | Ja: faalt in elke CI-run, ook bij de automatische retry (2/2 pogingen) |
| Status | Open |

## Precondities
De test maakt zelf een cursus met unieke titel (run-id), via de API als admin:
- `draftCourseIsNotListed`: cursus aangemaakt maar **niet gepubliceerd** (status `DRAFT`);
- `cancelledCourseIsNotListed`: cursus gepubliceerd en daarna **geannuleerd** (status `CANCELLED`).

## Stappen om te reproduceren
1. Open `https://qacademyb.duckdns.org/` zonder in te loggen.
2. Vul in het zoekveld (`filter-q-input`) de unieke titel van de testcursus in.
3. Klik op **Search** (`apply-filters-button`).

## Verwacht resultaat
Volgens US-01 en de API-specificatie ("DRAFT/CANCELLED/COMPLETED courses never appear here, regardless of filters"):
geen kaart voor de cursus en de melding **"No courses match your filters."**

## Actueel resultaat
De kaart `course-card-<id>` van de DRAFT- of CANCELLED-cursus wordt getoond.

```
Locator expected to have count: 0
Received: 1
  - waiting for locator("[data-testid=\"course-card-eff40906-66e9-4158-9b22-c820623ca28a\"]")
    19 × locator resolved to 1 element
```

Ook in de seed-data zichtbaar: de demo-cursus **"Security Testing Essentials (draft)"** (status `DRAFT`) staat in de publieke catalogus.

## Bewijs
- CI: workflow *Web tests (Playwright)*, job `chromium / catalog-auth-admin`, gefaald in elke run sinds de eerste (o.a. run 43736030984).
- Trace + screenshot: artifact `web-traces-chromium-catalog-auth-admin-<run>` → `CatalogBrowseTest-draftCourseIsNotListed-*.zip`
  (openen met `npx playwright show-trace <zip>` of op https://trace.playwright.dev).
- Rechtstreeks: `GET /api/public/courses?q=<titel>` geeft de DRAFT-cursus terug.

## Impact
Bezoekers zien cursussen die nog niet klaar zijn (onvolledige data, geen datums of locatie) of die geannuleerd zijn, en openen er het detail van. Ook alle opgeruimde (geannuleerde) testcursussen blijven publiek zichtbaar.
