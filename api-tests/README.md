# QAcademy API-tests - team 2

Postman-collectie, testplan en CI-pipeline voor de REST API van QAcademy (team 2: `https://qacademyb.duckdns.org`). Gemaakt door Gijs Colson, Flor Martens en Rube Stifter (PE1).
**17 testcases (5 scenario's), 129 requests, alle 34 user stories (US-01 t/m US-34) gedekt.**

> Deze map is in PE2 verplaatst vanuit de root van de repository (`git mv`, historiek behouden). De bugrapporten staan nu in [`../bug-reports/api/`](../bug-reports/api/).

## Inhoud

```
api-tests/
├── package.json, package-lock.json          Newman 6.2.3 + htmlextra (npm ci / npm test)
├── postman/qacademy-team2.postman_collection.json
├── postman/qacademy-team2.postman_environment.json   bevat GEEN wachtwoord (zie hieronder)
├── testplan/Testplan_QAcademy_Team2.xlsx    Overzicht, Testplan, Stappen, Dekking, Testdata, Bugs, Prestatie
└── LOGBOEK.md                               werkverdeling en tijdsregistratie per teamlid (PE1)
../.github/workflows/api-tests.yml           pipeline
../bug-reports/api/                          BUG-01..04, PERF-01 en sjablonen
```

## Testcase of request?

- **Testcase** = een doel (blad *Testplan*, TC01-TC17) = een **map** in Postman.
- **Request** = een HTTP-call in die map (blad *Stappen*, ID `TC05.03`), met meerdere asserties.
- Blad *Dekking*: per user story welke testcases ze testen (34 van 34).

| TC | Onderwerp | Type |
|---|---|---|
| 01-02 | Publieke catalogus: filteren, paginering, sortering, ongeldige invoer | enkel |
| 03 | Publiek detail en curriculum | enkel |
| 04 | Authenticatie en autorisatie (401/403) | enkel |
| 05 | Locatiebeheer | **scenario** |
| 06 | Cursus aanmaken en lezen | enkel |
| 07 | Modules | enkel |
| 08 | Cursus-levenscyclus | **scenario** |
| 09-11 | Inschrijven, betalen, annuleren (bouwen op elkaar voort) | enkel |
| 12 | Capaciteit: HARD_CAP en onbetaalde plaatsen | **scenario** |
| 13 | Wachtlijst en doorschuiven | **scenario** |
| 14 | Aanwezigheden | enkel |
| 15 | Examenresultaten | enkel |
| 16 | Gebruikersbeheer incl. in/uitschakelen en inloggen | **scenario** |
| 17 | Performance: checkout 5x gemeten | enkel (niet-functioneel) |

Extra mappen: `00 Setup` (S1-S6 accounts activeren en ID's opzoeken, S7 testdata) en `99 Opruimen` (C1).

## Wachtwoord

Het wachtwoord van de testgebruikers staat **niet** in de repository. Het environment heeft een lege `password`.

- **Postman:** vul `password` in als *current value* in het environment (wordt niet gesynchroniseerd of gecommit).
- **Newman / CI:** het wachtwoord komt uit de omgevingsvariabele `QA_PASSWORD` (in GitHub: repository secret `QA_PASSWORD`).

## Newman lokaal

```bash
cd api-tests
npm ci
QA_PASSWORD='...' npm test          # rapport in api-tests/reports/report.html
```

Windows PowerShell: `$env:QA_PASSWORD='...'; npm test`

## Pipeline

Workflow **API tests (Postman / Newman)** (`.github/workflows/api-tests.yml`):

- start bij een push of pull request die iets in `api-tests/` (of de workflow zelf) wijzigt, en manueel via *Run workflow* (keuze van de server, standaard team 2);
- deelt een concurrency-groep met de web-tests, zodat de twee suites nooit tegelijk tegen dezelfde omgeving draaien (C1 wist alle inschrijvingen van het team);
- publiceert altijd het artifact `newman-report-<nr>` (HTML, JUnit, JSON) en een samenvatting in de job.

Verwacht na een run (16 tot 22 van de 445 tests rood, allemaal door SUT-bugs): BUG-01 (TC01.01, TC01.02, TC08.10, TC08.15), BUG-02 (TC13.05), BUG-03 (TC08.03), BUG-04 (TC05.02 plus 5 gevolgfouten in TC05) en PERF-01 (TC17.01). Andere rode tests zijn meestal een aanname of testfout: kijk eerst daar.

## Onderhoudbaarheid

- Tokens per rol via de header `X-Role` (collection pre-request, automatisch vernieuwd).
- Testdata per run (`run_id`), niets hardgecodeerd. Accounts, URLs, drempels en `date_suffix` staan in het environment, het wachtwoord in een secret.
- Gelijkaardige negatieve gevallen zitten als lijst in 1 request; elke regel is een aparte test in het rapport.
- `perf_threshold_ms` (3000) geldt voor elk request; TC17 toetst checkout strenger (`perf_checkout_max_ms` 500, `perf_spread_ms` 300).

## Probleemoplossing

| Probleem | Oplossing |
|---|---|
| S1-S5 falen met 401 | Token ophalen mislukt: is `QA_PASSWORD` gezet (of `password` in Postman)? Controleer ook `keycloak_url`. |
| S6: "Gebruiker ... gevonden" faalt | E-mailadres bevat de username niet; bekijk `GET /api/admin/users` en pas de zoekregel in S6 aan. |
| S7 faalt met 400 op datums | Zet `date_suffix` in het environment op een lege waarde. |
| Veel tests falen na een fout in S7 of TC09 | Normaal: afhankelijke tests. Los de eerste fout op. |
| Teamgenoot test tegelijk | Afspreken: C1 (reset-test-data) wist ook hun inschrijvingen en die van lopende web-tests. |
