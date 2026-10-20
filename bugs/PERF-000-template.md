# PERF-000: Trage en wisselende responstijd op <endpoint>

| Veld | Waarde |
|---|---|
| Type | Niet-functioneel / performance |
| Omgeving | Team 2 - https://qacademyb.duckdns.org |
| Endpoint | <bv. GET /api/admin/courses/{id}/subscribers> |
| Gemeten door | Newman, test "Responstijd" (4 pipeline-runs) |

## Metingen
| Endpoint | Run 1 | Run 2 | Run 3 | Run 4 | Min | Max |
|---|---|---|---|---|---|---|
| <endpoint> | ms | ms | ms | ms | ms | ms |
| <ter vergelijking: een ander endpoint> | ms | ms | ms | ms | ms | ms |

## Bevinding
<welk endpoint is opvallend trager en/of sterk wisselend, in vergelijking met de andere endpoints>

## Verwacht
Responstijd onder <drempel> ms en stabiel (spreiding onder <drempel> ms).

## Impact
<wat betekent dit voor gebruikers bij meer verkeer>
