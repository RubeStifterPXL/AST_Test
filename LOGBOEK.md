# Logboek – QAcademy API-tests (team 2)

**Team:** Gijs Colson, Flor Martens, Rube Stifter  
**Opdracht:** REST API van QAcademy testen met Postman, Newman en GitHub Actions  
**SUT:** https://qacademyb.duckdns.org (Keycloak: https://qacademy-auth.duckdns.org, realm `qacademy`)

## 1. Werkverdeling en tijd

| # | Taak | Uitgevoerd door | Tijd |
|---|---|---|---|
| 1 | Swagger en user stories doorlezen, endpoints inventariseren (34 testbare US) | Gijs, Flor, Rube | 30 min elk |
| 2 | Testplan opstellen (17 testcases, technieken, dekking) | Flor | 40 min |
| 3 | Environment en collection-opzet (variabelen, tokenlogica, pre-request) | Flor | 20 min |
| 4 | Setup-keten (S1–S7) bouwen: testdata per run | Rube | 45 min |
| 5 | Testcases TC01–TC06 implementeren | Rube | 30 min |
| 6 | Testcases TC07–TC12 implementeren | Gijs | 40 min |
| 7 | Testcases TC13–TC17 implementeren | Flor | 30 min |
| 8 | Opruim-map (99 Opruimen) | Gijs | 25 min |
| 9 | Runs uitvoeren en resultaten analyseren (Postman) | Gijs, Flor, Rube | 20 min elk |
| 10 | Bugrapporten schrijven (BUG-01 t/m BUG-04, PERF-01) | Flor (BUG-02, PERF-01), Gijs (BUG-01, BUG-04), Rube (BUG-03) | Flor 25 min, Gijs 15 min, Rube 10 min |
| 11 | GitHub-repo opzetten, collaborator toevoegen | Gijs | 25 min |
| 12 | GitHub Actions-workflow en Newman-rapport als artifact | Rube | 15 min |
| 13 | Testplan-xlsx afwerken (resultaten, blad Prestatie) | Gijs, Flor, Rube | 15 min elk |
| 14 | README en dit logboek afwerken | Gijs, Flor, Rube | 10 min elk |
| 15 | Eindcontrole voor inlevering | Gijs, Flor, Rube | 20 min elk |

## 2. Totaal per persoon

| Teamlid | Totale tijd |
|---|---|
| Gijs Colson | 3 u 20 min (200 min) |
| Flor Martens | 3 u 30 min (210 min) |
| Rube Stifter | 3 u 15 min (195 min) |
| **Team** | **10 u 05 min (605 min)** |

## 3. Ontdekkingen tijdens het testen

- Een gebruiker staat pas in de users-tabel na zijn eerste API-call. De setup laat daarom elke account eerst één call doen.
- Capaciteit telt enkel CONFIRMED-inschrijvingen (volgens US-04, dus geen bug).
- Een betaalde inschrijving annuleren geeft geen refund (volgens US-06, dus geen bug).

## 4. Gevonden bugs

- BUG-01: de publieke catalogus toont DRAFT- en CANCELLED-cursussen.
- BUG-02: de wachtlijst promoveert de jongste inschrijving in plaats van de oudste.
- BUG-03: een `PUT` cursus met onbekende `locationId` geeft 404 in plaats van 400.
- BUG-04: `POST /api/admin/locations` met `roomCapacity: 0` geeft 400, terwijl de Swagger min 0 toelaat. Veroorzaakt 5 gevolgfouten in TC05.
- PERF-01: de checkout duurt 170–2003 ms, de andere endpoints rond 100 ms.

Een eerste kandidaat-bug rond het verwijderen van een locatie in gebruik bleek een setupfout en is ingetrokken.

## 5. Problemen en beslissingen

- `reset-test-data` wist ook data van teamgenoten, dus we spreken af wie wanneer test.
- Testdata maken we per run zelf aan met een unieke `run_id`. Seed-cursussen wijzigen we niet.
- Het aantal falende tests verschilt per run (16 tot 22) door de wisselende checkout-responstijd (PERF-01). De andere 16 fails zijn vast.

## 6. Hulpmiddelen

- Postman en Newman 6 (htmlextra-reporter)
- GitHub en GitHub Actions
- Claude (AI-assistent) voor het opzetten van de collection, het analyseren van runs en het schrijven van de bugrapporten
