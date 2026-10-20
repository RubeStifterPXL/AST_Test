# QAcademy API-tests - team 2 (samengevoegde uitwerking)

Postman-collectie, testplan, bugrapporten en CI-pipeline voor de REST API van QAcademy (team 2: `https://qacademyb.duckdns.org`).
**17 testcases (5 scenario's), 129 requests, alle 34 user stories (US-01 t/m US-34) gedekt.**

Deze versie combineert de eigen uitwerking (volledige dekking, 17 testcases + dekkingsblad) met de bevindingen van een collega die al tegen de server gedraaid heeft: account-activatie in de setup, een gerichte checkout-performancetest, de bugrapporten en een betere pipeline.

## Inhoud

```
.github/workflows/api-tests.yml      pipeline (manueel starten, Newman, rapport + samenvatting)
package.json, package-lock.json      Newman 6.2.3 + htmlextra (npm ci / npm test)
postman/qacademy-team2.postman_collection.json
postman/qacademy-team2.postman_environment.json
testplan/Testplan_QAcademy_Team2.xlsx   Overzicht, Testplan, Stappen, Dekking, Testdata, Bugs, Prestatie
bugs/BUG-01,02,03,05, PERF-01       bugrapporten (bevestigd met eigen run van 20/10/2026), BUG-000/PERF-000 = sjablonen
LOGBOEK.md                           logboek (namen en uren nog invullen)
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

## Draaien in Postman

1. Import beide `.json`-bestanden uit `postman/`, kies het environment **QAcademy team 2**.
2. Rechtsklik op de collectie > **Run collection** > volgorde laten staan > **Run**.
3. Rode tests = kandidaat-bugs of aannames die niet kloppen (blad Testplan, kolom K).

Verwacht na een run (19 van 445 tests rood, allemaal door SUT-bugs): BUG-01 (TC01.01, TC01.02, TC08.10, TC08.15), BUG-02 (TC13.05), BUG-03 (TC08.03), BUG-05 (TC05.02 plus 5 gevolgfouten in TC05) en PERF-01 (TC17.01). Andere rode tests zijn meestal een aanname of testfout: kijk eerst daar.

## Newman lokaal en pipeline

```bash
npm ci
npm test            # rapport in reports/report.html
```

GitHub: private repository, `custersnele` en teamgenoten als collaborator, daarna Actions > **API tests (Postman / Newman)** > Run workflow. Artifact `newman-report-<nr>` bevat het HTML-rapport (ook bij een rode build).

## Onderhoudbaarheid

- Tokens per rol via de header `X-Role` (collection pre-request, automatisch vernieuwd).
- Testdata per run (`run_id`), niets hardgecodeerd. Accounts, URLs, drempels en `date_suffix` staan in het environment.
- Gelijkaardige negatieve gevallen zitten als lijst in 1 request; elke regel is een aparte test in het rapport.
- `perf_threshold_ms` (3000) geldt voor elk request; TC17 toetst checkout strenger (`perf_checkout_max_ms` 500, `perf_spread_ms` 300).

## Wat jij nog moet doen

1. Run in Postman en nakijken welke tests rood zijn.
2. Resultaat per testcase invullen (Pass/Fail, Bug-ID) in blad Testplan; blad Bugs en `bugs/` aanpassen aan jullie eigen run (bewijs van de collega vervangen).
3. Blad Prestatie invullen met de checkout-tijden uit 3-4 pipeline-runs.
4. LOGBOEK invullen, repo aanmaken en pipeline draaien.

## Probleemoplossing

| Probleem | Oplossing |
|---|---|
| S1-S5 falen met 401 | Token ophalen mislukt: Postman Console openen, `keycloak_url` en `password` controleren. |
| S6: "Gebruiker ... gevonden" faalt | E-mailadres bevat de username niet; bekijk `GET /api/admin/users` en pas de zoekregel in S6 aan. |
| S7 faalt met 400 op datums | Zet `date_suffix` in het environment op een lege waarde. |
| Veel tests falen na een fout in S7 of TC09 | Normaal: afhankelijke tests. Los de eerste fout op. |
| Teamgenoot test tegelijk | Afspreken: C1 (reset-test-data) wist ook hun inschrijvingen. |
