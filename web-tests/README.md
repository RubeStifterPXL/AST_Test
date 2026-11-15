# QAcademy web-tests (Playwright for Java)

End-to-end browsertests voor de web interface van QAcademy (team 2), tegen de echte backend, database en Keycloak.
Java 21, Playwright 1.49, JUnit 5, AssertJ, PDFBox (certificaat-PDF lezen), Jackson (API-setup).

## Snel starten

```bash
cd web-tests
cp .env.example .env              # vul QA_PASSWORD in (staat in .gitignore)
mvn exec:java@install-browsers    # eenmalig: Chromium + OS-afhankelijkheden
mvn test                          # volledige suite, headless
```

| Wat | Commando |
|---|---|
| Eén groep (JUnit-tag) | `mvn test -Dgroups=catalog` · `-Dgroups="student \| certificates"` · `-Dgroups=e2e` |
| Eén user story | `mvn test -Dgroups=US-12` |
| Eén klasse / methode | `mvn test -Dtest=CertificateTest` · `-Dtest=CertificateTest#downloadedPdfContainsAllDetails` |
| Andere browser | `BROWSER=firefox mvn test` (eerst `mvn exec:java@install-browsers -Dbrowser.install=firefox`) |
| Browser zichtbaar | `HEADLESS=false SLOW_MO_MS=300 mvn test -Dtest=LoginLogoutTest` |
| Geen retry | `mvn test -Dretries=0` |
| HTML-rapport | `mvn surefire-report:report-only site -DgenerateReports=false` → `target/reports/surefire.html` |
| Trace bekijken | `npx playwright show-trace target/artifacts/traces/<test>.zip` of https://trace.playwright.dev |

Windows PowerShell: `$env:BROWSER='firefox'; mvn test`.

## Configuratie

Alles via omgevingsvariabelen (CI: GitHub secrets/variables) of `web-tests/.env` lokaal; dezelfde code draait lokaal en in CI.

| Variabele | Standaard | Doel |
|---|---|---|
| `QA_PASSWORD` | **verplicht**, geen standaard | wachtwoord van de testgebruikers |
| `BASE_URL` | `https://qacademyb.duckdns.org` | frontend + API (team 2) |
| `KEYCLOAK_URL` | `https://qacademy-auth.duckdns.org` | Keycloak |
| `BROWSER` | `chromium` | `chromium`, `firefox`, `webkit` |
| `HEADLESS` / `SLOW_MO_MS` | `true` / `0` | debuggen |
| `TIMEOUT_MS` | `15000` | timeout voor acties en asserties |
| `TRACE_ALWAYS` | `false` | ook traces van geslaagde tests bewaren |
| `USER_ADMIN`, `USER_INSTRUCTOR`, `USER_STUDENT_1..3` | testgebruikers uit de opdracht | andere accounts gebruiken |

## Structuur en design patterns

```
src/test/java/be/pxl/qacademy/
├── config/      Config (env/.env), TestUser (rollen en accounts)
├── api/         ApiClient (Keycloak-token per gebruiker), QAcademyApi (setup/opruimen via REST)
├── factories/   CourseBuilder (Builder), TestCourse, TestDataTracker (opruimen), RunId (unieke data)
├── fixtures/    BaseUiTest (browser, context per gebruiker, cleanup), AuthState (ingelogde sessie per rol),
│                FailureArtifacts (trace + screenshot bij falen)
├── pages/       Page Objects: één klasse per pagina + NavBar + KeycloakLoginPage
├── support/     @Author, PdfText
└── tests/       per epic: catalog, auth, student, certificates, instructor, admin, e2e
```

- **Page Object Model**: tests bevatten geen locators. Elke pagina is een klasse die enkel `data-testid`-locators gebruikt (de standaard van Playwright, robuust tegen tekst- en stijlwijzigingen).
- **Builder / Factory**: `aCourse().titled("Waitlist").waitlist().maxSubscribers(1).withModule(..).create()` maakt een eigen cursus via de API (aanmaken, modules, locatie, datums, instructor, publiceren) en registreert ze voor opruiming.
- **Fixtures**: `pageAs(TestUser.STUDENT_1)` geeft een pagina die al ingelogd is. `AuthState` logt per rol één keer echt in via de Keycloak-pagina en hergebruikt de cookies (storage state in `target/.auth`, gitignored). `anonymousPage()` geeft een pagina zonder login.
- **Scheiding**: pagina's (`pages/`), testdata (`factories/`, `api/`) en tests (`tests/`).

## Testdata: onafhankelijk en herhaalbaar

- Elke test maakt **zijn eigen cursus** met titel/slug `QA <run-id> <label> <uniek>`; `instructor.test` wordt docent. Zoekopdrachten in de catalogus gebruiken die unieke titel, zodat data van teamgenoten of andere pipelines het resultaat niet beïnvloedt.
- De demo-cursussen worden enkel **gelezen** (`CatalogBrowseTest`, `CourseDetailTest.demoCourseDetail`).
- Na elke test ruimt `TestDataTracker` op: cursus verwijderen, of annuleren als ze inschrijvingen had (verwijderen is dan geblokkeerd, US-16). Aangemaakte gebruikers worden uitgeschakeld (er is geen delete).
- Eén locatie per run (`QA Campus <run-id>`), gedeeld door alle cursussen van die run.
- **Nooit** *Admin → Reset test data* in een test: dat wist de inschrijvingen van het hele team. `AdminHomePage` heeft er bewust geen methode voor.
- Setup via de API faalt luid (`expectOk()`) met de echte HTTP-fout, zodat een setupfout nooit als SUT-bug in het rapport staat.

## User stories en tests

Elke testklasse heeft `@Tag("US-xx")`, een `@Tag` per epic en `@Author`. Het team verdeelt de klassen: vervang `Authors.TO_ASSIGN` door je naam en commit dat **vanuit je eigen account**, zodat de git-historiek en de annotatie hetzelfde zeggen.

| US | Story | Testklasse | Tests | Auteur |
|---|---|---|---|---|
| US-01 | Gepubliceerde cursussen bekijken | `catalog/CatalogBrowseTest` | 6 | |
| US-02 | Filteren (zoekterm, prijs, gratis, leeftijd, upcoming, combinaties, grenswaarden) | `catalog/CatalogFilterTest` | 16 | |
| US-03 | Bladeren door de pagina's | `catalog/CatalogPaginationTest` | 5 | |
| US-04 | Cursusdetail met modules | `catalog/CourseDetailTest` | 7 | |
| US-05 | Inloggen en uitloggen (geldig/ongeldig) | `auth/LoginLogoutTest` | 4 | |
| US-06 | Student: geen admin/instructor-toegang (ook rechtstreekse URL's) | `auth/StudentAuthorizationTest` | 12 | |
| US-07 | Instructor: geen admin-toegang | `auth/InstructorAuthorizationTest` | 8 | |
| US-08 | Inschrijven: gratis en betaald (+ betalen) | `student/SubscribeTest` | 5 | |
| US-09 | Eigen inschrijvingen, aanwezigheden, resultaten | `student/MyOverviewTest` | 5 | |
| US-10 | Volle cursus: HARD_CAP en WAITLIST | `student/CapacityTest` | 4 | |
| US-11 | Annuleren en doorschuiven wachtlijst | `student/CancelSubscriptionTest` | 5 | |
| US-12 | Certificaat bekijken en PDF downloaden | `certificates/CertificateTest` | 6 | |
| US-13 | My courses: enkel toegewezen cursussen | `instructor/MyCoursesTest` | 4 | |
| US-14 | Inschrijvingen zonder e-mailadressen | `instructor/InstructorEnrollmentsTest` | 4 | |
| US-15 | Aanwezigheid en resultaten, enkel eigen cursus | `instructor/InstructorAttendanceAndResultsTest` | 7 | |
| US-16 | Cursussen en modules beheren | `admin/AdminCourseManagementTest` | 9 | |
| US-17 | Locaties en gebruikers beheren | `admin/AdminLocationTest`, `admin/AdminUserTest` | 5 + 7 | |
| E2E | Levenscyclus cursus (2 modules, max 2, inschrijven, aanwezigheid, resultaat, certificaat) | `e2e/CourseLifecycleE2ETest` | 1 (7 stappen) | Team |

Autorisatie per rol wordt systematisch getest: per rol de navbar, rechtstreekse URL's naar elke admin-, instructor- en studentpagina (geparametriseerd) en de controle dat de juiste rol wél binnen kan.

## Verwachte rode tests (SUT-bugs)

Een test faalt alleen door een bug in de toepassing. Elke rode test heeft een bugrapport in [`../bug-reports/web/`](../bug-reports/web/):

| Test | Bug |
|---|---|
| `CatalogBrowseTest.draftCourseIsNotListed`, `cancelledCourseIsNotListed` | [WEB-BUG-01](../bug-reports/web/WEB-BUG-01-catalog-shows-draft-and-cancelled-courses.md) |
| `InstructorEnrollmentsTest.instructorDoesNotSeeEmailAddresses` | [WEB-BUG-02](../bug-reports/web/WEB-BUG-02-instructor-sees-student-emails.md) |
| `CancelSubscriptionTest.freedSeatGoesToOldestWaitlistedStudent` | [WEB-BUG-03](../bug-reports/web/WEB-BUG-03-waitlist-promotes-newest-student.md) |
| `CancelSubscriptionTest.cancelPendingPayment` | [WEB-BUG-04](../bug-reports/web/WEB-BUG-04-no-cancel-button-for-pending-payment.md) |

## Tonen dat een test iets controleert

Branch `experiment/mutation-check` verandert in 5 tests bewust de verwachte waarde (catalogusfilter, PDF-inhoud, aanwezigheid, autorisatie, E2E-certificaat). Die 5 tests worden daar rood; alle andere uitkomsten blijven gelijk. Die branch wordt **niet** gemerged.

## Probleemoplossing

| Probleem | Oplossing |
|---|---|
| `QA_PASSWORD is niet gezet` | `web-tests/.env` aanmaken of de omgevingsvariabele zetten. |
| `Setup-request mislukt: ... HTTP 401` | Wachtwoord of Keycloak-URL fout; client `qacademy-test-tools` moet password grant toelaten. |
| `Gebruiker ... niet gevonden` | Het account moet minstens één keer ingelogd hebben; `QAcademyApi.userId` doet dat zelf. |
| Nav-links "niet zichtbaar" | Venster te smal: de navbar klapt in onder 992 px. De tests gebruiken 1440 x 900. |
| Alles traag of time-outs | Draait een teamgenoot tegelijk met een reset? Afspreken, of de concurrency-groep in CI laten werken. |
