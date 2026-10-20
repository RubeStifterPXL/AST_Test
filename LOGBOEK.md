# Logboek – Automated API Testing (QAcademy)

**Team:** 2 (qacademyb) · **Teamleden:** <naam 1>, <naam 2>, <naam 3>
**Repository:** <link naar GitHub-repo>

## Hoe we dit logboek bijhouden
- Na elke werksessie één regel per persoon in het **Overzicht**: datum, wie, hoe lang, wat.
- Per sessie een korte entry in **Detail**: wat we deden, wat we ontdekten, problemen, beslissingen en volgende stappen.
- Bugs en beslissingen verwijzen naar hun ID (BUG-xx, TC-xx, SC-xx), zodat je ze terugvindt in het testplan en de bugrapporten.
- Hulpmiddelen (Postman, Newman, AI-assistent, documentatie) vermelden we eerlijk bij de sessie waarin we ze gebruikten.

---

## Overzicht

| Datum | Teamlid | Uren | Activiteit | Fase | Resultaat / link |
|---|---|---|---|---|---|
| 12/10/2026 | <naam> | <u> | Opdracht en rubrics gelezen, API verkend via Swagger en Postman | Verkenning | Lijst met kandidaat-bugs |
| 12/10/2026 | <naam> | <u> | Eerste versie van testplan, collectie, bugrapporten en pipeline opgezet | Implementatie | `testplan/`, `postman/`, `bugreports/` |
| | | | | | |
| | | | | | |
| **Totaal** | | **0** | | | |

---

## Detail

### 12/10/2026 – Verkenning van de API
**Aanwezig:** <namen> · **Duur:** <u>

**Wat gedaan**
- Opdracht, rubrics en user stories (US-01..34) doorgenomen.
- OpenAPI-spec van `https://qacademyb.duckdns.org/v3/api-docs` bekeken.
- Tokens opgehaald voor alle 5 testaccounts en de belangrijkste flows handmatig uitgeprobeerd: catalogus, inschrijven, betalen, annuleren, cursus aanmaken en publiceren, modules, aanwezigheden, examens.
- Responstijden per endpoint gemeten.

**Ontdekkingen**
- Een gebruiker staat pas in de users-tabel na zijn eerste API-call. De Setup moet elke account dus eerst één call laten doen.
- Capaciteit telt enkel CONFIRMED-inschrijvingen. Dat is volgens US-04 correct gedrag, dus geen bug.
- Een betaalde inschrijving annuleren geeft geen refund. Ook dat is volgens US-06 correct gedrag.

**Kandidaat-bugs**
- BUG-01: de publieke catalogus toont DRAFT- en CANCELLED-cursussen.
- BUG-02: de wachtlijst promoveert de jongste inschrijving in plaats van de oudste.
- BUG-03: een `PUT` cursus met onbekende `locationId` geeft 404 in plaats van 400.
- BUG-05: `POST /api/admin/locations` met `roomCapacity: 0` geeft 400 ("roomCapacity must be greater than 0"), terwijl de Swagger min 0 toelaat. Veroorzaakt 5 gevolgfouten in TC05.
- PERF-01: de checkout duurt 170–1930 ms; de andere endpoints zitten rond 100 ms. Eigen run: 917–1626 ms, wisselvallig.
- BUG-04 (locatie in gebruik toch verwijderbaar) was géén SUT-bug: een fout in onze setup (loc_ref te vroeg uitgelezen), verholpen. De SUT geeft correct 409.

**Problemen / beslissingen**
- `reset-test-data` wist ook de data van teamgenoten → we spreken af wie wanneer test.
- Testdata: we maken per run eigen cursussen aan met een unieke `runId` en ruimen ze op in de Teardown. Seed-cursussen wijzigen we niet.

**Hulpmiddelen**
- Postman, curl, Swagger UI, AI-assistent (Claude) voor de verkenning en een eerste opzet.

**Volgende stappen**
- [ ] Testplan nalezen en aanvullen
- [ ] Collectie in Postman importeren en zelf doorlopen
- [ ] GitHub-repo aanmaken (privé, custersnele toevoegen)
- [ ] Pipeline in GitHub Actions draaien

---

### <datum> – <onderwerp>
**Aanwezig:** · **Duur:**

**Wat gedaan**
-

**Ontdekkingen**
-

**Problemen / beslissingen**
-

**Hulpmiddelen**
-

**Volgende stappen**
- [ ]
