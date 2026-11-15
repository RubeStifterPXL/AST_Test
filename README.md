# QAcademy Test Automation Suite - team 2

[![API tests](https://github.com/RubeStifterPXL/AST_Test/actions/workflows/api-tests.yml/badge.svg)](https://github.com/RubeStifterPXL/AST_Test/actions/workflows/api-tests.yml)
[![Web tests](https://github.com/RubeStifterPXL/AST_Test/actions/workflows/web-tests.yml/badge.svg)](https://github.com/RubeStifterPXL/AST_Test/actions/workflows/web-tests.yml)

Volledige testautomatisatie (TAS) voor **QAcademy**, het cursusplatform van team 2 (`https://qacademyb.duckdns.org`):
Angular-frontend, Spring Boot REST API, PostgreSQL en Keycloak.
Team: Gijs Colson, Flor Martens, Rube Stifter.

> Een rode badge betekent hier niet "kapotte tests": elke rode test wijst op een gedocumenteerde bug in de SUT (zie [bug-reports/](bug-reports/)).

## Inhoud

```
.
├── README.md                     dit overzicht
├── .github/workflows/
│   ├── api-tests.yml             Newman, push/PR op api-tests/** + manueel
│   └── web-tests.yml             Playwright, push/PR op web-tests/** + manueel + nachtelijk
├── api-tests/                    PE1: Postman-collectie, testplan, logboek (Node/Newman)   -> api-tests/README.md
├── web-tests/                    PE2: Playwright for Java (Maven)                          -> web-tests/README.md
└── bug-reports/                  één bestand per bug, API én web                           -> bug-reports/README.md
```

`api-tests/` en `web-tests/` zijn onafhankelijke projecten (eigen build-bestand, eigen afhankelijkheden, eigen pipeline). Een trage browsertest blokkeert de API-tests niet.

## Lokaal draaien

| Suite | Vereisten | Commando |
|---|---|---|
| API | Node 20 | `cd api-tests && npm ci && QA_PASSWORD=... npm test` |
| Web | Java 21, Maven 3.9 | `cd web-tests && cp .env.example .env` (QA_PASSWORD invullen), `mvn exec:java@install-browsers`, `mvn test` |

## CI (GitHub Actions)

| | API tests | Web tests |
|---|---|---|
| Triggers | push op `main` en PR die `api-tests/**` wijzigt, *Run workflow* | push op `main` en PR die `web-tests/**` wijzigt, *Run workflow* (browser + server kiezen), elke nacht 02:17 UTC (alle browsers) |
| Parallel | 1 job | browser-matrix (Chromium, Firefox, WebKit) x 3 shards per JUnit-tag |
| Retries | - | max 1 (`-Dretries=1`); een test die pas na een retry slaagt staat als **flaky** in de samenvatting |
| Faalt bij | falende assertie | falende test (`mvn` exit code) |
| Rapport (altijd) | `newman-report-<nr>`: HTML, JUnit, JSON | `web-report-<browser>-<shard>-<nr>`: HTML (surefire), JUnit-XML; `web-traces-...`: Playwright-traces + screenshots van gefaalde tests |
| Samenvatting | tabel per run in de job summary | tabel per job: totalen, gefaalde tests, flaky tests |

Beide workflows delen de concurrency-groep `qacademy-environment`: ze draaien nooit tegelijk tegen dezelfde omgeving (de API-suite reset aan het einde de testdata van het team).

### Geheimen en variabelen

Geen wachtwoorden, tokens of IP's in de repository. Instellen in *Settings → Secrets and variables → Actions*:

| Naam | Soort | Verplicht | Waarde |
|---|---|---|---|
| `QA_PASSWORD` | secret | ja | wachtwoord van de testgebruikers |
| `QA_BASE_URL` | variable | nee | standaard `https://qacademyb.duckdns.org` |
| `QA_KEYCLOAK_URL` | variable | nee | standaard `https://qacademy-auth.duckdns.org` |

`.gitignore` sluit `target/`, `node_modules/`, rapporten, traces, screenshots, `.auth/` en `.env` uit.

## Werkwijze

- Werk op een feature-branch en open een pull request naar `main`; merge enkel met groene pipeline (of met rode tests die enkel gedocumenteerde SUT-bugs zijn).
  Aanbevolen: *Settings → Branches → Add rule* op `main` met "Require a pull request" en "Require status checks to pass".
- Elke testklasse draagt `@Author`; commit je eigen tests vanuit je eigen GitHub-account.
- Nieuwe bug: kopieer het sjabloon in `bug-reports/web/` of `bug-reports/api/` en voeg een rij toe in `bug-reports/README.md`.
